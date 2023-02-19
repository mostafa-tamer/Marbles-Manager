package com.example.barcodereader.fragments.mainMenuFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.get.groups.Groups
import com.example.barcodereader.userData
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.launch
import retrofit2.Response

class MainMenuViewModel(private val userDao: UserDao) : ScanViewModel(userDao) {

    val logoutStatus = Observable(true)
    val loginStatus = MutableLiveData(true)
    var groupsPills = Observable<Response<Groups>>()

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

    fun branchesPills() {
        viewModelScope.launch {
            try {
                val api = Api(userData.subBaseURL)

                val response = api.call.getBranches(
                    userData.schema,
                    userData.loginCount,
                    userData.employeeNumber
                )

                if (response.isSuccessful) {
                    groupsPills.setValue(
                        response
                    )
                } else {
                    throw Exception("Error Occurred: " + response.code())
                }
                connectionStatus.setValue(true)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
                println(e.message)
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