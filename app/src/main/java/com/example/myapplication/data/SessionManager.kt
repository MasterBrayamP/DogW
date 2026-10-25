package com.example.myapplication.data

import com.example.myapplication.data.datastore.UserPreferencesManager
import kotlinx.coroutines.runBlocking

class SessionManager(private val userPreferencesManager: UserPreferencesManager) {

    private var cachedUserId: String? = null

    init {
        cachedUserId = runBlocking { userPreferencesManager.getCurrentUserId() }
    }

    fun login(userId: String) {
        cachedUserId = userId
        runBlocking { userPreferencesManager.saveCurrentUserId(userId) }
    }

    fun logout() {
        cachedUserId = null
        runBlocking { userPreferencesManager.clearSession() }
    }

    fun currentUserId(): String? = cachedUserId
}
