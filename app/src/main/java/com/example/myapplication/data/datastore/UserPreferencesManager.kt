package com.example.myapplication.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.Role
import com.example.myapplication.data.User
import com.example.myapplication.data.Walk
import com.example.myapplication.data.WalkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dog_walker_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        private val KEY_CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        private val KEY_USERS_JSON = stringPreferencesKey("users_json")
        private val KEY_WALKS_JSON = stringPreferencesKey("walks_json")
    }

    val currentUserIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_USER_ID]
    }

    suspend fun getCurrentUserId(): String? = currentUserIdFlow.first()

    suspend fun saveCurrentUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_CURRENT_USER_ID)
        }
    }

    val usersFlow: Flow<List<User>> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_USERS_JSON] ?: return@map emptyList()
        deserializeUsers(raw)
    }

    suspend fun loadUsers(): List<User> {
        val raw = context.dataStore.data.first()[KEY_USERS_JSON] ?: return emptyList()
        return deserializeUsers(raw)
    }

    suspend fun saveUsers(users: List<User>) {
        val jsonString = serializeUsers(users)
        context.dataStore.edit { preferences ->
            preferences[KEY_USERS_JSON] = jsonString
        }
    }

    val walksFlow: Flow<List<Walk>> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_WALKS_JSON] ?: return@map emptyList()
        deserializeWalks(raw)
    }

    suspend fun loadWalks(): List<Walk> {
        val raw = context.dataStore.data.first()[KEY_WALKS_JSON] ?: return emptyList()
        return deserializeWalks(raw)
    }

    suspend fun saveWalks(walks: List<Walk>) {
        val jsonString = serializeWalks(walks)
        context.dataStore.edit { preferences ->
            preferences[KEY_WALKS_JSON] = jsonString
        }
    }

    private fun serializeUsers(users: List<User>): String {
        val arr = JSONArray()
        users.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("email", it.email)
                    .put("password", it.password)
                    .put("role", it.role.name)
            )
        }
        return arr.toString()
    }

    private fun deserializeUsers(raw: String): List<User> {
        val list = mutableListOf<User>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list += User(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    email = o.getString("email"),
                    password = o.getString("password"),
                    role = Role.valueOf(o.getString("role"))
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun serializeWalks(walks: List<Walk>): String {
        val arr = JSONArray()
        walks.forEach { w ->
            val dogs = JSONArray()
            w.dogIds.forEach { dogs.put(it) }
            arr.put(
                JSONObject()
                    .put("id", w.id)
                    .put("ownerId", w.ownerId)
                    .put("dogIds", dogs)
                    .put("date", w.date)
                    .put("time", w.time)
                    .put("durationMin", w.durationMin)
                    .put("notes", w.notes)
                    .put("status", w.status.name)
                    .put("walkerId", w.walkerId)
                    .put("walkerName", w.walkerName)
                    .put("startedAt", w.startedAt)
                    .put("elapsedSec", w.elapsedSec)
                    .put("distanceKm", w.distanceKm)
                    .put("speedKmh", w.speedKmh)
                    .put("progress", w.progress.toDouble())
            )
        }
        return arr.toString()
    }

    private fun deserializeWalks(raw: String): List<Walk> {
        val list = mutableListOf<Walk>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val dogIds = mutableListOf<String>()
                val dogs = o.getJSONArray("dogIds")
                for (j in 0 until dogs.length()) dogIds += dogs.getString(j)
                list += Walk(
                    id = o.getString("id"),
                    ownerId = o.getString("ownerId"),
                    dogIds = dogIds,
                    date = o.getString("date"),
                    time = o.getString("time"),
                    durationMin = o.getInt("durationMin"),
                    notes = o.optString("notes"),
                    status = WalkStatus.valueOf(o.getString("status")),
                    walkerId = o.optString("walkerId").ifBlank { null },
                    walkerName = o.optString("walkerName").ifBlank { null },
                    startedAt = if (o.has("startedAt") && !o.isNull("startedAt")) o.getLong("startedAt") else null,
                    elapsedSec = o.optInt("elapsedSec"),
                    distanceKm = o.optDouble("distanceKm"),
                    speedKmh = o.optDouble("speedKmh"),
                    progress = o.optDouble("progress").toFloat()
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
