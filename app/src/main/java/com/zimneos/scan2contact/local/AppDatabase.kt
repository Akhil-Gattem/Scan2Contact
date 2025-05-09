package com.zimneos.scan2contact.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zimneos.scan2contact.core.Converters
import com.zimneos.scan2contact.viewmodel.UserProfile

@Database(entities = [UserProfile::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}