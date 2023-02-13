package com.example.barcodereader.fragments.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.get.marble.Marble
import com.example.barcodereader.utils.AESEncryption
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class ScanViewModel(private val dataSource: UserDao) : ViewModel() {

    val connectionStatus = MutableLiveData(true)
    val barcode = Observable("")
    val marble = Observable<Response<Marble>>()

    fun retUser() = dataSource.retUser()

    fun retRetrofitData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {

        val encryptedBarcode = AESEncryption.encrypt(barcode, GlobalKeys.KEY)
        val encryptedEmployeeNumber = AESEncryption.encrypt(employeeNo, GlobalKeys.KEY)

        viewModelScope.launch {
            try {

                withContext(Dispatchers.IO) {
                    val user = dataSource.retUserSuspend()
                    val api = Api(user[0].subBaseURL)

                    val response = api.call.getBarcode(
                        schema, encryptedBarcode, loginCount, encryptedEmployeeNumber
                    )

                    withContext(Dispatchers.Main) {
                        marble.setValue(response)
                        connectionStatus.setValue(true)
                    }
                }

            } catch (e: Exception) {
                println("Exception in ResultFragmentViewModel => retRetrofitData(): " + e.message)
                connectionStatus.setValue(true)
            }
        }
    }
}