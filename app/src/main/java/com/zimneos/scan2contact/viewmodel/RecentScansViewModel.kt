package com.zimneos.scan2contact.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zimneos.scan2contact.data.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RecentScansViewModel(private val repository: UserProfileRepository) : ViewModel() {

    private val _userProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val userProfiles: StateFlow<List<UserProfile>> = _userProfiles

    private val _designations = MutableStateFlow<List<String>>(emptyList())
    val designations: StateFlow<List<String>> = _designations

    private var allProfiles: List<UserProfile> = emptyList()

    init {
        loadUserProfiles()
    }

    private fun loadUserProfiles() {
        viewModelScope.launch {
            repository.getAllUserProfiles().collect { profiles ->
                allProfiles = profiles
                _userProfiles.value = profiles
                _designations.value = profiles
                    .mapNotNull { it.designation }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            }
        }
    }

    fun filterProfiles(selectedDesignations: List<String>, startDate: LocalDateTime?, endDate: LocalDateTime?) {
        viewModelScope.launch {
            _userProfiles.value = allProfiles.filter { profile ->
                val matchesDesignation = if (selectedDesignations.isEmpty()) {
                    true
                } else {
                    profile.designation?.let { it in selectedDesignations } == true
                }

                val matchesDateRange = if (startDate != null && endDate != null) {
                    profile.scannedTime?.let { scannedTime ->
                        !scannedTime.isBefore(startDate) && !scannedTime.isAfter(endDate)
                    } == true
                } else {
                    true
                }

                matchesDesignation && matchesDateRange
            }
        }
    }

    fun filterByName(query: String) {
        viewModelScope.launch {
            _userProfiles.value = if (query.isBlank()) {
                allProfiles
            } else {
                allProfiles.filter { profile ->
                    profile.name?.contains(query, ignoreCase = true) == true
                }
            }
        }
    }

    fun resetFilters() {
        viewModelScope.launch {
            _userProfiles.value = allProfiles
        }
    }

    fun addUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.upsertUserProfile(userProfile)
        }
    }

    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.upsertUserProfile(userProfile)
        }
    }

    fun deleteUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.deleteUserProfile(userProfile)
        }
    }

    fun getUserProfileById(id: String, onResult: (UserProfile?) -> Unit) {
        viewModelScope.launch {
            val profile = repository.getUserProfileById(id)
            onResult(profile)
        }
    }

    fun deleteAllUserProfiles() {
        viewModelScope.launch {
            repository.deleteAllUserProfiles()
        }
    }
}
