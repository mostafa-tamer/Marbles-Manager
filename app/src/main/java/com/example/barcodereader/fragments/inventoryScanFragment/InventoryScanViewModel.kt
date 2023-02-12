package com.example.barcodereader.fragments.inventoryScanFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.udacity.asteroidradar.database.UserDao

class InventoryScanViewModel(private val userDao: UserDao) : ScanViewModel(userDao) {


    class InventoryScanViewModelFactory(
        private val userDao: UserDao
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryScanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventoryScanViewModel(userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}