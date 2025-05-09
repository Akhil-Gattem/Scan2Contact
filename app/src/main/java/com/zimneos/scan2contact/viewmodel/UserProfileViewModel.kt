package com.zimneos.scan2contact.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zimneos.scan2contact.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val detectedTexts: List<String> = emptyList(),
    val scannedImage: Uri? = null,
    val scannedTime: LocalDateTime? = null,
    val name: String? = null,
    val designation: String? = null,
    val phone: String? = null,
    val secondaryPhone: String? = null,
    val tertiaryPhone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val website: String? = null
)

class UserProfileViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    fun update(scannedImageUri: Uri) {
        viewModelScope.launch {
            val profile = repository.getUserProfileByScannedImage(scannedImageUri.toString())
            if (profile != null) {
                _userProfile.value = profile
            } else {
                _userProfile.value = UserProfile(scannedImage = scannedImageUri)
                repository.upsertUserProfile(_userProfile.value)
            }
        }
    }

    fun updateUserProfile(newProfile: UserProfile) {
        viewModelScope.launch {
            _userProfile.value = newProfile
            repository.upsertUserProfile(newProfile)
        }
    }

    fun updateDetectedTexts(newDetectedTexts: List<String>) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(detectedTexts = newDetectedTexts)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(name = newName)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateScannedTime(newScannedTime: LocalDateTime) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(scannedTime = newScannedTime)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateDesignation(newDesignation: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(designation = newDesignation)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updatePhone(newPhone: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(phone = newPhone)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateSecondaryPhone(newSecondaryPhone: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(secondaryPhone = newSecondaryPhone)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateTertiaryPhone(newTertiaryPhone: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(tertiaryPhone = newTertiaryPhone)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(email = newEmail)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(address = newAddress)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun updateWebsite(newWebsite: String) {
        viewModelScope.launch {
            _userProfile.value = _userProfile.value.copy(website = newWebsite)
            repository.upsertUserProfile(_userProfile.value)
        }
    }

    fun deleteUserProfile() {
        viewModelScope.launch {
            _userProfile.value = UserProfile()
            repository.deleteUserProfile(_userProfile.value)
        }
    }
}
