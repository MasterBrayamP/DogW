package com.example.myapplication.data

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID
import kotlin.math.min
import kotlin.random.Random

class AppRepository(private val store: PrefsStore) {

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
        users += store.loadUsers()
        dogs += store.loadDogs()
        walks += store.loadWalks()
        if (users.isEmpty()) seed()
        publish()
        resumeTrackingIfNeeded()
    }

    fun users(): List<User> = users.toList()
    fun dogs(): List<Dog> = dogs.toList()
    fun walks(): List<Walk> = walks.toList()

    fun userById(id: String) = users.find { it.id == id }
    fun userByEmail(email: String) = users.find { it.email.equals(email.trim(), true) }
    fun dogById(id: String) = dogs.find { it.id == id }
    fun walkById(id: String) = walks.find { it.id == id }

    fun dogsOf(ownerId: String) = dogs.filter { it.ownerId == ownerId }
    fun walksOfOwner(ownerId: String) = walks.filter { it.ownerId == ownerId }.sortedByDescending { it.id }
    fun pendingWalks() = walks.filter { it.status == WalkStatus.PENDING }.sortedByDescending { it.id }

    fun register(name: String, email: String, password: String, role: Role): User {
        val user = User(UUID.randomUUID().toString(), name.trim(), email.trim(), password, role)
        users += user
        store.saveUsers(users)
        return user
    }

    fun addDog(dog: Dog) {
        dogs += dog
        store.saveDogs(dogs)
        publish()
    }

    fun updateDog(dog: Dog) {
        val i = dogs.indexOfFirst { it.id == dog.id }
        if (i >= 0) {
            dogs[i] = dog
            store.saveDogs(dogs)
            publish()
        }
    }

    fun createWalk(
        ownerId: String,
        dogIds: List<String>,
        date: String,
        time: String,
        durationMin: Int,
        notes: String
    ): Walk {
        val walk = Walk(
            id = UUID.randomUUID().toString(),
            ownerId = ownerId,
            dogIds = dogIds,
            date = date,
            time = time,
            durationMin = durationMin,
            notes = notes,
            status = WalkStatus.PENDING
        )
        walks += walk
        persistWalks()
        return walk
    }

    fun acceptWalk(walkId: String, walker: User) {
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

    fun rejectWalk(walkId: String) {
        updateWalk(walkId) { it.copy(status = WalkStatus.REJECTED) }
    }

    fun completeWalk(walkId: String) {
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

    private fun updateWalk(id: String, transform: (Walk) -> Walk) {
        val i = walks.indexOfFirst { it.id == id }
        if (i >= 0) {
            walks[i] = transform(walks[i])
            persistWalks()
        }
    }

    private fun persistWalks() {
        store.saveWalks(walks)
        _walksLive.value = walks.toList()
    }

    private fun publish() {
        _dogsLive.value = dogs.toList()
        _walksLive.value = walks.toList()
    }

    private fun seed() {
        val ana = User("u-ana", "Ana", "ana@example.com", "123456", Role.OWNER)
        val juan = User("u-juan", "Juan", "juan@example.com", "123456", Role.WALKER)
        users += ana
        users += juan
        dogs += Dog(
            id = "d-luna",
            ownerId = ana.id,
            name = "Luna",
            breed = "Golden Retriever",
            age = 3,
            weightKg = 24,
            personality = "Cariñosa y juguetona",
            hasDisability = true,
            condition = "Displasia de cadera leve, no puede correr largas distancias.",
            avatarColor = "#EAB308"
        )
        dogs += Dog(
            id = "d-rocky",
            ownerId = ana.id,
            name = "Rocky",
            breed = "Beagle",
            age = 2,
            weightKg = 12,
            personality = "Activo y curioso",
            hasDisability = false,
            condition = "",
            avatarColor = "#A16207"
        )
        store.saveUsers(users)
        store.saveDogs(dogs)
        store.saveWalks(walks)
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
                }
            }
            persistWalks()
            handler.postDelayed(this, 1000)
        }
    }
}
