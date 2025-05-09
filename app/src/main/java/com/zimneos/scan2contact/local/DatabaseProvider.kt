package com.zimneos.scan2contact.local

import android.content.Context
import androidx.room.Room
import com.zimneos.scan2contact.data.UserProfileRepository

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "scan2contact_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getUserProfileRepository(context: Context): UserProfileRepository {
        return UserProfileRepository(getDatabase(context).userProfileDao())
    }
}