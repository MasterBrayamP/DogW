package com.example.myapplication.sqllite

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import androidx.core.database.sqlite.transaction
import com.example.myapplication.data.Dog
import com.example.myapplication.data.Role
import com.example.myapplication.data.User
import com.example.myapplication.data.Walk
import com.example.myapplication.data.WalkStatus

class DbDogWalker(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOMBRE_BD, null, VERSION_BD) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T_USUARIOS (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre     VARCHAR(100) NOT NULL,
                correo     VARCHAR(150) NOT NULL UNIQUE COLLATE NOCASE,
                contrasena VARCHAR(100) NOT NULL,
                rol        VARCHAR(20)  NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $T_PERROS (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                dueno_id            INTEGER NOT NULL,
                nombre              VARCHAR(100) NOT NULL,
                raza                VARCHAR(100) NOT NULL,
                edad                INTEGER NOT NULL,
                peso_kg             INTEGER NOT NULL,
                personalidad        VARCHAR(200) NOT NULL DEFAULT '',
                tiene_discapacidad  INTEGER NOT NULL DEFAULT 0,
                condicion           VARCHAR(300) NOT NULL DEFAULT '',
                color_avatar        VARCHAR(9)   NOT NULL DEFAULT '#22C55E',
                FOREIGN KEY (dueno_id) REFERENCES $T_USUARIOS(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $T_PASEOS (
                id                      INTEGER PRIMARY KEY AUTOINCREMENT,
                dueno_id                INTEGER NOT NULL,
                fecha                   VARCHAR(10)  NOT NULL,
                hora                    VARCHAR(5)   NOT NULL,
                duracion_min            INTEGER NOT NULL,
                notas                   VARCHAR(500) NOT NULL DEFAULT '',
                estado                  VARCHAR(20)  NOT NULL,
                paseador_id             INTEGER,
                paseador_nombre         VARCHAR(100),
                iniciado_en             INTEGER,
                segundos_transcurridos  INTEGER NOT NULL DEFAULT 0,
                distancia_km            REAL NOT NULL DEFAULT 0,
                velocidad_kmh           REAL NOT NULL DEFAULT 0,
                progreso                REAL NOT NULL DEFAULT 0,
                FOREIGN KEY (dueno_id)    REFERENCES $T_USUARIOS(id),
                FOREIGN KEY (paseador_id) REFERENCES $T_USUARIOS(id)
            )
            """.trimIndent()
        )
        // Un paseo puede llevar varios perros
        db.execSQL(
            """
            CREATE TABLE $T_PASEO_PERROS (
                paseo_id  INTEGER NOT NULL,
                perro_id  INTEGER NOT NULL,
                posicion  INTEGER NOT NULL,
                PRIMARY KEY (paseo_id, perro_id),
                FOREIGN KEY (paseo_id) REFERENCES $T_PASEOS(id) ON DELETE CASCADE,
                FOREIGN KEY (perro_id) REFERENCES $T_PERROS(id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_perros_dueno ON $T_PERROS(dueno_id)")
        db.execSQL("CREATE INDEX idx_paseos_dueno ON $T_PASEOS(dueno_id)")
        db.execSQL("CREATE INDEX idx_paseos_estado ON $T_PASEOS(estado)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        listOf(
            T_PASEO_PERROS, T_PASEOS, T_PERROS, T_USUARIOS,
            "walk_dogs", "walks", "dogs", "users"
        ).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }


    fun insertarUsuario(u: User): Long =
        writableDatabase.insertOrThrow(
            T_USUARIOS, null, contentValuesOf(
                "nombre" to u.name,
                "correo" to u.email,
                "contrasena" to u.password,
                "rol" to u.role.name
            )
        )

    fun listarUsuarios(): MutableList<User> {
        val lista = mutableListOf<User>()
        readableDatabase.query(T_USUARIOS, null, null, null, null, null, "id").use { c ->
            while (c.moveToNext()) {
                lista += User(
                    id = c.largo("id"),
                    name = c.texto("nombre"),
                    email = c.texto("correo"),
                    password = c.texto("contrasena"),
                    role = Role.valueOf(c.texto("rol"))
                )
            }
        }
        return lista
    }


    fun insertarPerro(d: Dog): Long =
        writableDatabase.insertOrThrow(T_PERROS, null, valoresPerro(d))

    fun actualizarPerro(d: Dog) {
        writableDatabase.update(T_PERROS, valoresPerro(d), "id = ?", arrayOf(d.id.toString()))
    }

    fun listarPerros(): MutableList<Dog> {
        val lista = mutableListOf<Dog>()
        readableDatabase.query(T_PERROS, null, null, null, null, null, "id").use { c ->
            while (c.moveToNext()) {
                lista += Dog(
                    id = c.largo("id"),
                    ownerId = c.largo("dueno_id"),
                    name = c.texto("nombre"),
                    breed = c.texto("raza"),
                    age = c.entero("edad"),
                    weightKg = c.entero("peso_kg"),
                    personality = c.texto("personalidad"),
                    hasDisability = c.entero("tiene_discapacidad") == 1,
                    condition = c.texto("condicion"),
                    avatarColor = c.texto("color_avatar")
                )
            }
        }
        return lista
    }

    private fun valoresPerro(d: Dog) = contentValuesOf(
        "dueno_id" to d.ownerId,
        "nombre" to d.name,
        "raza" to d.breed,
        "edad" to d.age,
        "peso_kg" to d.weightKg,
        "personalidad" to d.personality,
        "tiene_discapacidad" to if (d.hasDisability) 1 else 0,
        "condicion" to d.condition,
        "color_avatar" to d.avatarColor
    )

    fun insertarPaseo(w: Walk): Long =
        writableDatabase.transaction {
            val paseoId = insertOrThrow(T_PASEOS, null, valoresPaseo(w))
            w.dogIds.forEachIndexed { i, perroId ->
                insertOrThrow(
                    T_PASEO_PERROS, null, contentValuesOf(
                        "paseo_id" to paseoId,
                        "perro_id" to perroId,
                        "posicion" to i
                    )
                )
            }
            paseoId
        }

    fun actualizarPaseo(w: Walk) {
        writableDatabase.update(T_PASEOS, valoresPaseo(w), "id = ?", arrayOf(w.id.toString()))
    }

    fun listarPaseos(): MutableList<Walk> {
        val db = readableDatabase

        // paseo_id -> perros en orden
        val perrosPorPaseo = mutableMapOf<Long, MutableList<Long>>()
        db.query(T_PASEO_PERROS, null, null, null, null, null, "paseo_id, posicion").use { c ->
            while (c.moveToNext()) {
                perrosPorPaseo.getOrPut(c.largo("paseo_id")) { mutableListOf() } += c.largo("perro_id")
            }
        }

        val lista = mutableListOf<Walk>()
        db.query(T_PASEOS, null, null, null, null, null, "id").use { c ->
            while (c.moveToNext()) {
                val id = c.largo("id")
                lista += Walk(
                    id = id,
                    ownerId = c.largo("dueno_id"),
                    dogIds = perrosPorPaseo[id] ?: emptyList(),
                    date = c.texto("fecha"),
                    time = c.texto("hora"),
                    durationMin = c.entero("duracion_min"),
                    notes = c.texto("notas"),
                    status = WalkStatus.valueOf(c.texto("estado")),
                    walkerId = c.largoONulo("paseador_id"),
                    walkerName = c.textoONulo("paseador_nombre"),
                    startedAt = c.largoONulo("iniciado_en"),
                    elapsedSec = c.entero("segundos_transcurridos"),
                    distanceKm = c.decimal("distancia_km"),
                    speedKmh = c.decimal("velocidad_kmh"),
                    progress = c.decimal("progreso").toFloat()
                )
            }
        }
        return lista
    }

    private fun valoresPaseo(w: Walk) = contentValuesOf(
        "dueno_id" to w.ownerId,
        "fecha" to w.date,
        "hora" to w.time,
        "duracion_min" to w.durationMin,
        "notas" to w.notes,
        "estado" to w.status.name,
        "paseador_id" to w.walkerId,
        "paseador_nombre" to w.walkerName,
        "iniciado_en" to w.startedAt,
        "segundos_transcurridos" to w.elapsedSec,
        "distancia_km" to w.distanceKm,
        "velocidad_kmh" to w.speedKmh,
        "progreso" to w.progress.toDouble()
    )


    private fun Cursor.col(nombre: String) = getColumnIndexOrThrow(nombre)
    private fun Cursor.texto(nombre: String): String = getString(col(nombre)) ?: ""
    private fun Cursor.entero(nombre: String): Int = getInt(col(nombre))
    private fun Cursor.largo(nombre: String): Long = getLong(col(nombre))
    private fun Cursor.decimal(nombre: String): Double = getDouble(col(nombre))
    private fun Cursor.textoONulo(nombre: String): String? =
        col(nombre).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.largoONulo(nombre: String): Long? =
        col(nombre).let { if (isNull(it)) null else getLong(it) }

    companion object {
        const val NOMBRE_BD = "dogwalker.db"
        const val VERSION_BD = 2

        const val T_USUARIOS = "usuarios"
        const val T_PERROS = "perros"
        const val T_PASEOS = "paseos"
        const val T_PASEO_PERROS = "paseo_perros"
    }
}
