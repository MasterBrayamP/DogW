package com.example.myapplication.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PrefsStore(context: Context) {
    private val prefs = context.getSharedPreferences("dog_walker_data", Context.MODE_PRIVATE)

    fun loadUsers(): MutableList<User> {
        val raw = prefs.getString(KEY_USERS, null) ?: return mutableListOf()
        val list = mutableListOf<User>()
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
        return list
    }

    fun loadDogs(): MutableList<Dog> {
        val raw = prefs.getString(KEY_DOGS, null) ?: return mutableListOf()
        val list = mutableListOf<Dog>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list += Dog(
                id = o.getString("id"),
                ownerId = o.getString("ownerId"),
                name = o.getString("name"),
                breed = o.getString("breed"),
                age = o.getInt("age"),
                weightKg = o.getInt("weightKg"),
                personality = o.optString("personality"),
                hasDisability = o.optBoolean("hasDisability"),
                condition = o.optString("condition"),
                avatarColor = o.optString("avatarColor", "#22C55E")
            )
        }
        return list
    }

    fun loadWalks(): MutableList<Walk> {
        val raw = prefs.getString(KEY_WALKS, null) ?: return mutableListOf()
        val list = mutableListOf<Walk>()
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
        return list
    }

    fun saveUsers(users: List<User>) {
        val arr = JSONArray()
        users.forEach {
            arr.put(JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("email", it.email)
                .put("password", it.password)
                .put("role", it.role.name))
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply()
    }

    fun saveDogs(dogs: List<Dog>) {
        val arr = JSONArray()
        dogs.forEach {
            arr.put(JSONObject()
                .put("id", it.id)
                .put("ownerId", it.ownerId)
                .put("name", it.name)
                .put("breed", it.breed)
                .put("age", it.age)
                .put("weightKg", it.weightKg)
                .put("personality", it.personality)
                .put("hasDisability", it.hasDisability)
                .put("condition", it.condition)
                .put("avatarColor", it.avatarColor))
        }
        prefs.edit().putString(KEY_DOGS, arr.toString()).apply()
    }

    fun saveWalks(walks: List<Walk>) {
        val arr = JSONArray()
        walks.forEach { w ->
            val dogs = JSONArray()
            w.dogIds.forEach { dogs.put(it) }
            arr.put(JSONObject()
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
                .put("progress", w.progress.toDouble()))
        }
        prefs.edit().putString(KEY_WALKS, arr.toString()).apply()
    }

    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_DOGS = "dogs"
        private const val KEY_WALKS = "walks"
    }
}
