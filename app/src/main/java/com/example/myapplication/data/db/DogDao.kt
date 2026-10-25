package com.example.myapplication.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.Dog
import kotlinx.coroutines.flow.Flow

@Dao
interface DogDao {
    @Query("SELECT * FROM dogs")
    fun getAllDogsFlow(): Flow<List<Dog>>

    @Query("SELECT * FROM dogs")
    suspend fun getAllDogs(): List<Dog>

    @Query("SELECT * FROM dogs WHERE ownerId = :ownerId")
    fun getDogsByOwnerFlow(ownerId: String): Flow<List<Dog>>

    @Query("SELECT * FROM dogs WHERE ownerId = :ownerId")
    suspend fun getDogsByOwner(ownerId: String): List<Dog>

    @Query("SELECT * FROM dogs WHERE id = :id")
    suspend fun getDogById(id: String): Dog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDog(dog: Dog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDogs(dogs: List<Dog>)

    @Update
    suspend fun updateDog(dog: Dog)

    @Delete
    suspend fun deleteDog(dog: Dog)

    @Query("DELETE FROM dogs WHERE id = :id")
    suspend fun deleteDogById(id: String)
}
