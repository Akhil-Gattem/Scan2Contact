package com.zimneos.scan2contact.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.zimneos.scan2contact.viewmodel.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles")
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserProfileById(id: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE scannedImage = :imgId")
    suspend fun getUserProfileByScannedImage(imgId: String): UserProfile?

    @Upsert
    suspend fun upsertUserProfile(userProfile: UserProfile)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfile)

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllUserProfiles()
}