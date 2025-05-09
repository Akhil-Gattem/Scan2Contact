package com.zimneos.scan2contact.data

import com.zimneos.scan2contact.local.UserProfileDao
import com.zimneos.scan2contact.viewmodel.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    fun getAllUserProfiles(): Flow<List<UserProfile>> {
        return userProfileDao.getAllUserProfiles()
    }

    suspend fun getUserProfileById(id: String): UserProfile? {
        return userProfileDao.getUserProfileById(id.toLong().toString())
    }

    suspend fun getUserProfileByScannedImage(imgId: String): UserProfile? {
        return userProfileDao.getUserProfileByScannedImage(imgId)
    }

    suspend fun upsertUserProfile(userProfile: UserProfile) {
        userProfileDao.upsertUserProfile(userProfile)
    }

    suspend fun deleteUserProfile(userProfile: UserProfile) {
        userProfileDao.deleteUserProfile(userProfile)
    }

    suspend fun deleteAllUserProfiles() {
        userProfileDao.deleteAllUserProfiles()
    }
}