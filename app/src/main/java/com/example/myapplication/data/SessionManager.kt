package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dog_walker_session", Context.MODE_PRIVATE)

    fun login(userId: String) {
        prefs.edit().putString(KEY_USER, userId).apply()
    }

    fun logout() {
        prefs.edit().remove(KEY_USER).apply()
    }

    fun currentUserId(): String? = prefs.getString(KEY_USER, null)

    companion object {
        private const val KEY_USER = "user_id"
    }
}
