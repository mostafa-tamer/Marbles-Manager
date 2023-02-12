package com.example.barcodereader.fragments.scanFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.get.Statistics
import com.example.barcodereader.utils.AESEncryption
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import com.udacity.asteroidradar.database.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ScanFragmentViewModel(private val dataSource: UserDao) : ViewModel() {

    val logoutStatus = MutableLiveData(true)
    val barcode = Observable("")
    val statistics = Observable<Response<Statistics>>()

    fun logout() {
        viewModelScope.launch {
            try {
                dataSource.logout()
                logoutStatus.setValue(true)
            } catch (e: Exception) {
                logoutStatus.setValue(false)
            }
        }
    }

    fun decrypt(value: String): String {
        return AESEncryption.decrypt(value, GlobalKeys.KEY)
    }

    fun retUser() = dataSource.retUser()

    fun retRetrofitData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = dataSource.retUserSuspend()
                    val api = Api(user[0].subBaseURL)

                    val response = api.call.getBarcode(schema, barcode, loginCount, employeeNo)

                    withContext(Dispatchers.Main) {
                        statistics.setValue(response)
                    }
                }
            } catch (e: Exception) {
                println("Exception in ResultFragmentViewModel => retRetrofitData(): " + e.message)
            }
        }
    }

    class ScanFragmentViewModelFactory(private val dataSource: UserDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScanFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ScanFragmentViewModel(dataSource) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}