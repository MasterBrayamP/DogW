package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

//Sesión del usuario guardada en SharedPreferences.
class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dog_walker_session", Context.MODE_PRIVATE)

    fun login(userId: Long) {
        prefs.edit().putLong(KEY_USER, userId).apply()
    }

    fun logout() {
        prefs.edit().remove(KEY_USER).apply()
    }

    fun currentUserId(): Long? =
        if (prefs.contains(KEY_USER)) prefs.getLong(KEY_USER, -1L) else null

    //Último correo con el que se inició sesión (se conserva al cerrar sesión).
    fun saveLastEmail(email: String) {
        prefs.edit().putString(KEY_LAST_EMAIL, email.trim()).apply()
    }

    fun lastEmail(): String = prefs.getString(KEY_LAST_EMAIL, "").orEmpty()

    companion object {
        // Clave nueva (el id ahora es Long; la anterior guardaba un String)
        private const val KEY_USER = "usuario_id"
        private const val KEY_LAST_EMAIL = "last_email"
    }
}
