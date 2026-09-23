package com.example.myapplication.data

enum class Role { OWNER, WALKER }

enum class WalkStatus {
    PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, REJECTED
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: Role
)

data class Dog(
    val id: String,
    val ownerId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val weightKg: Int,
    val personality: String,
    val hasDisability: Boolean,
    val condition: String,
    val avatarColor: String
)

data class Walk(
    val id: String,
    val ownerId: String,
    val dogIds: List<String>,
    val date: String,
    val time: String,
    val durationMin: Int,
    val notes: String,
    val status: WalkStatus,
    val walkerId: String? = null,
    val walkerName: String? = null,
    val startedAt: Long? = null,
    val elapsedSec: Int = 0,
    val distanceKm: Double = 0.0,
    val speedKmh: Double = 0.0,
    val progress: Float = 0f
)
