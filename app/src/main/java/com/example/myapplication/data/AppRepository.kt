package com.example.myapplication.data

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.sqllite.DbDogWalker
import kotlin.math.min
import kotlin.random.Random

class AppRepository(private val db: DbDogWalker) {

    private val users = mutableListOf<User>()
    private val dogs = mutableListOf<Dog>()
    private val walks = mutableListOf<Walk>()

    private val _dogsLive = MutableLiveData<List<Dog>>(emptyList())
    private val _walksLive = MutableLiveData<List<Walk>>(emptyList())
    val dogsLive: LiveData<List<Dog>> = _dogsLive
    val walksLive: LiveData<List<Walk>> = _walksLive

    private val handler = Handler(Looper.getMainLooper())
    private var tracking = false

    fun init() {
        users.clear()
        dogs.clear()
        walks.clear()
        users += db.listarUsuarios()
        if (users.isEmpty()) seed()
        dogs += db.listarPerros()
        walks += db.listarPaseos()
        publish()
        resumeTrackingIfNeeded()
    }

    fun users(): List<User> = users.toList()
    fun dogs(): List<Dog> = dogs.toList()
    fun walks(): List<Walk> = walks.toList()

    fun userById(id: Long?) = users.find { it.id == id }
    fun userByEmail(email: String) = users.find { it.email.equals(email.trim(), true) }
    fun dogById(id: Long) = dogs.find { it.id == id }
    fun walkById(id: Long) = walks.find { it.id == id }

    fun dogsOf(ownerId: Long) = dogs.filter { it.ownerId == ownerId }
    fun walksOfOwner(ownerId: Long) = walks.filter { it.ownerId == ownerId }.sortedByDescending { it.id }
    fun pendingWalks() = walks.filter { it.status == WalkStatus.PENDING }.sortedByDescending { it.id }

    fun register(name: String, email: String, password: String, role: Role): User {
        val nuevo = User(0, name.trim(), email.trim(), password, role)
        val user = nuevo.copy(id = db.insertarUsuario(nuevo))
        users += user
        return user
    }

    fun addDog(dog: Dog) {
        dogs += dog.copy(id = db.insertarPerro(dog))
        publish()
    }

    fun updateDog(dog: Dog) {
        val i = dogs.indexOfFirst { it.id == dog.id }
        if (i >= 0) {
            dogs[i] = dog
            db.actualizarPerro(dog)
            publish()
        }
    }

    fun createWalk(
        ownerId: Long,
        dogIds: List<Long>,
        date: String,
        time: String,
        durationMin: Int,
        notes: String
    ): Walk {
        val nuevo = Walk(
            id = 0,
            ownerId = ownerId,
            dogIds = dogIds,
            date = date,
            time = time,
            durationMin = durationMin,
            notes = notes,
            status = WalkStatus.PENDING
        )
        val walk = nuevo.copy(id = db.insertarPaseo(nuevo))
        walks += walk
        _walksLive.value = walks.toList()
        return walk
    }

    fun acceptWalk(walkId: Long, walker: User) {
        updateWalk(walkId) {
            it.copy(
                status = WalkStatus.IN_PROGRESS,
                walkerId = walker.id,
                walkerName = walker.name,
                startedAt = System.currentTimeMillis(),
                elapsedSec = 0,
                distanceKm = 0.0,
                speedKmh = 4.2,
                progress = 0f
            )
        }
        startTracking()
    }

    fun rejectWalk(walkId: Long) {
        updateWalk(walkId) { it.copy(status = WalkStatus.REJECTED) }
    }

    fun completeWalk(walkId: Long) {
        updateWalk(walkId) {
            it.copy(status = WalkStatus.COMPLETED, progress = 1f, speedKmh = 0.0)
        }
        if (walks.none { it.status == WalkStatus.IN_PROGRESS }) stopTracking()
    }

    fun dogNames(walk: Walk): String =
        walk.dogIds.mapNotNull { id -> dogById(id)?.name }.joinToString(", ")

    fun hasDisability(walk: Walk): Boolean =
        walk.dogIds.any { id -> dogById(id)?.hasDisability == true }

    fun disabilityText(walk: Walk): String =
        walk.dogIds.mapNotNull { id ->
            dogById(id)?.takeIf { it.hasDisability }?.condition
        }.joinToString("\n")

    private fun updateWalk(id: Long, transform: (Walk) -> Walk) {
        val i = walks.indexOfFirst { it.id == id }
        if (i >= 0) {
            walks[i] = transform(walks[i])
            db.actualizarPaseo(walks[i])
            _walksLive.value = walks.toList()
        }
    }

    private fun publish() {
        _dogsLive.value = dogs.toList()
        _walksLive.value = walks.toList()
    }

    private fun seed() {
        val anaId = db.insertarUsuario(User(0, "Ana", "ana@example.com", "123456", Role.OWNER))
        db.insertarUsuario(User(0, "Juan", "juan@example.com", "123456", Role.WALKER))
        users += db.listarUsuarios()
        db.insertarPerro(
            Dog(
                id = 0,
                ownerId = anaId,
                name = "Luna",
                breed = "Golden Retriever",
                age = 3,
                weightKg = 24,
                personality = "Cariñosa y juguetona",
                hasDisability = true,
                condition = "Displasia de cadera leve, no puede correr largas distancias.",
                avatarColor = "#EAB308"
            )
        )
        db.insertarPerro(
            Dog(
                id = 0,
                ownerId = anaId,
                name = "Rocky",
                breed = "Beagle",
                age = 2,
                weightKg = 12,
                personality = "Activo y curioso",
                hasDisability = false,
                condition = "",
                avatarColor = "#A16207"
            )
        )
    }

    private fun resumeTrackingIfNeeded() {
        if (walks.any { it.status == WalkStatus.IN_PROGRESS }) startTracking()
    }

    private fun startTracking() {
        if (tracking) return
        tracking = true
        handler.post(tick)
    }

    private fun stopTracking() {
        tracking = false
        handler.removeCallbacks(tick)
    }

    private val tick = object : Runnable {
        override fun run() {
            if (!tracking) return
            val active = walks.filter { it.status == WalkStatus.IN_PROGRESS }
            if (active.isEmpty()) {
                stopTracking()
                return
            }
            active.forEach { walk ->
                val nextSec = walk.elapsedSec + 1
                val durationSec = 90
                val progress = min(1f, nextSec / durationSec.toFloat())
                val speed = 3.6 + Random.nextDouble() * 1.6
                val distance = walk.distanceKm + speed / 3600.0
                val i = walks.indexOfFirst { it.id == walk.id }
                if (i >= 0) {
                    val updated = walk.copy(
                        elapsedSec = nextSec,
                        progress = progress,
                        speedKmh = speed,
                        distanceKm = distance
                    )
                    walks[i] = if (progress >= 1f) {
                        updated.copy(status = WalkStatus.COMPLETED, speedKmh = 0.0, progress = 1f)
                    } else updated
                    db.actualizarPaseo(walks[i])
                }
            }
            _walksLive.value = walks.toList()
            handler.postDelayed(this, 1000)
        }
    }
}
