package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppRepository
import com.example.myapplication.data.SessionManager
import com.example.myapplication.data.datastore.UserPreferencesManager
import com.example.myapplication.data.db.AppDatabase
import com.example.myapplication.data.repository.DogRepository

class DogWalkerApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var dogRepository: DogRepository
        private set
    lateinit var userPreferencesManager: UserPreferencesManager
        private set
    lateinit var session: SessionManager
        private set
    lateinit var repo: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        dogRepository = DogRepository(database.dogDao())
        userPreferencesManager = UserPreferencesManager(this)

        session = SessionManager(userPreferencesManager)
        repo = AppRepository(dogRepository, userPreferencesManager)
        repo.init()
    }

    companion object {
        lateinit var instance: DogWalkerApp
            private set
    }
}
