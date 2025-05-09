package com.zimneos.scan2contact.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zimneos.scan2contact.local.DatabaseProvider

class RecentScansViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentScansViewModel::class.java)) {
            val repository = DatabaseProvider.getUserProfileRepository(context)
            @Suppress("UNCHECKED_CAST")
            return RecentScansViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}