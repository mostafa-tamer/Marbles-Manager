package com.example.barcodereader.fragments.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.get.marble.Marble
import com.example.barcodereader.userData
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class ScanViewModel(private val dataSource: UserDao) : ViewModel() {

    val connectionStatus = Observable(true)
    val barcode = Observable("")
    val marbles = Observable<Response<Marble>>()

    fun retUser() = dataSource.retUser()

    fun retRetrofitData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val api = Api(userData.subBaseURL)
                    val response = api.call.getBarcode(
                        schema, barcode, loginCount, employeeNo
                    )

                    withContext(Dispatchers.Main) {
                        marbles.setValue(response)
                        connectionStatus.setValue(true)
                    }
                }

            } catch (e: Exception) {
                println("Exception in ResultFragmentViewModel => retRetrofitData(): " + e.message)
                connectionStatus.setValue(false)
            }
        }
    }
}