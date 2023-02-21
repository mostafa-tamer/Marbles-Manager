package com.example.barcodeReader.fragments.welcomeFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barcodeReader.database.User
import com.example.barcodeReader.database.UserDao
import kotlinx.coroutines.runBlocking

class WelcomeFragmentViewModel(private val dataSource: UserDao) : ViewModel() {

    val status = MutableLiveData(true)

    fun retUserSuspend(): User? {
        return runBlocking {
            dataSource.retUserSuspend()
        }
    }

    class WelcomeFragmentViewModelFactory(private val dataSource: UserDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WelcomeFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WelcomeFragmentViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}