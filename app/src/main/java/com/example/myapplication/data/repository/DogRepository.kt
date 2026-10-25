package com.example.myapplication.data.repository

import com.example.myapplication.data.Dog
import com.example.myapplication.data.db.DogDao
import kotlinx.coroutines.flow.Flow

class DogRepository(private val dogDao: DogDao) {

    val allDogsFlow: Flow<List<Dog>> = dogDao.getAllDogsFlow()

    fun getDogsByOwnerFlow(ownerId: String): Flow<List<Dog>> =
        dogDao.getDogsByOwnerFlow(ownerId)

    suspend fun getAllDogs(): List<Dog> = dogDao.getAllDogs()

    suspend fun getDogsByOwner(ownerId: String): List<Dog> =
        dogDao.getDogsByOwner(ownerId)

    suspend fun getDogById(id: String): Dog? = dogDao.getDogById(id)

    suspend fun insertDog(dog: Dog) = dogDao.insertDog(dog)

    suspend fun insertDogs(dogs: List<Dog>) = dogDao.insertDogs(dogs)

    suspend fun updateDog(dog: Dog) = dogDao.updateDog(dog)

    suspend fun deleteDog(dog: Dog) = dogDao.deleteDog(dog)

    suspend fun deleteDogById(id: String) = dogDao.deleteDogById(id)
}
