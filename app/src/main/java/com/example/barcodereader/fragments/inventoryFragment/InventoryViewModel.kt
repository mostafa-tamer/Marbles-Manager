package com.example.barcodereader.fragments.inventoryFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.get.brances.Groups
import com.udacity.asteroidradar.database.UserDao
import kotlinx.coroutines.launch
import retrofit2.Response

class InventoryViewModel(private val userDao: UserDao) : ViewModel() {

    val connectionStatus = MutableLiveData(true)

    var groups = MutableLiveData<Response<Groups>>()

    fun getBranches() {
        viewModelScope.launch {
            try {
                val user = userDao.retUserSuspend()

                val api = Api(user[0].subBaseURL)

                groups.value = api.call.getBranches(user[0].schema)
                connectionStatus.setValue(true)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
            }
        }
    }

    class InventoryFragmentViewModelFactory(private val dataSource: UserDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return InventoryViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}