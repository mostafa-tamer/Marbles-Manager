package com.example.barcodereader.fragments.welcomeFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barcodereader.databaes.User
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.utils.GlobalKeys

class WelcomeFragmentViewModel(private val dataSource: UserDao) : ViewModel() {

    val status = MutableLiveData(true)

    fun retUser(): LiveData<List<User>>? {
        return try {
            status.value = true
            dataSource.retUser()
        } catch (e: Exception) {
            status.value = false
            return null
        }
    }

    fun decrypt(value: String): String {
        return AESEncryption.decrypt(value, GlobalKeys.KEY)
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