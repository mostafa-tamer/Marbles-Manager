package com.example.barcodereader.fragments.scanFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.example.barcodereader.utils.AESEncryption
import com.example.barcodereader.utils.GlobalKeys
import com.udacity.asteroidradar.database.UserDao
import kotlinx.coroutines.launch

class MainMenuViewModel(private val userDao: UserDao) : ScanViewModel(userDao) {

    val logoutStatus = MutableLiveData(true)

    fun logout() {
        viewModelScope.launch {
            try {
                userDao.logout()
                logoutStatus.setValue(true)
            } catch (e: Exception) {
                logoutStatus.setValue(false)
            }
        }
    }

    fun decrypt(value: String): String {
        return AESEncryption.decrypt(value, GlobalKeys.KEY)
    }


    class ScanFragmentViewModelFactory(private val dataSource: UserDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainMenuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return MainMenuViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}