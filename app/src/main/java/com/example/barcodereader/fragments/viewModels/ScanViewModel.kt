package com.example.barcodereader.fragments.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.RetrofitClient
import com.example.barcodereader.network.properties.get.marble.Marble
import com.example.barcodereader.userData
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.launch
import retrofit2.Response

abstract class ScanViewModel(private val dataSource: UserDao) : ViewModel() {

    val connectionStatus = Observable(true)
    val barcode = Observable("")
    val marbles = Observable<Response<Marble>>()

    fun retUser() = dataSource.retUser()

    fun retRetrofitData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {

        viewModelScope.launch {
            try {
                val response = RetrofitClient
                    .getApiInstance(userData.subBaseURL)
                    .getBarcode(
                        schema, barcode, loginCount, employeeNo
                    )

                marbles.setValue(response)
                connectionStatus.setValue(true)

            } catch (e: Exception) {
                println("Exception in ResultFragmentViewModel => retRetrofitData(): " + e.message)
                connectionStatus.setValue(false)
            }
        }
    }
}