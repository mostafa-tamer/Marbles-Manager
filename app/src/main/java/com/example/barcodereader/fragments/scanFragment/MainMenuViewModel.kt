package com.example.barcodereader.fragments.scanFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.example.barcodereader.network.Api
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.properties.get.groups.Groups
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import retrofit2.Response

class MainMenuViewModel(private val userDao: UserDao) : ScanViewModel(userDao) {

    val logoutStatus = MutableLiveData(true)
    var groups = Observable<Response<Groups>>()

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

    fun getBranches() {
        viewModelScope.launch {
            try {
                val user = userDao.retUserSuspend()
                val api = Api(user.subBaseURL)
                groups.setValue(api.call.getBranches(user.schema))
                connectionStatus.setValue(true)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
            }
        }
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