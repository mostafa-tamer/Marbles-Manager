package com.example.barcodeReader.fragments.mainMenuFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodeReader.EnPack.error
import com.example.barcodeReader.EnPack.error_occurred
import com.example.barcodeReader.EnPack.server_is_unreachable
import com.example.barcodeReader.database.UserDao
import com.example.barcodeReader.network.RetrofitClient
import com.example.barcodeReader.network.properties.get.groups.Groups
import com.example.barcodeReader.network.properties.get.marble.Marble
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.AlertDialogErrorMessage
import com.example.barcodeReader.utils.Observable
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.IOException


class MainMenuViewModel(private val userDao: UserDao) : ViewModel() {

    val barcodeObservable = Observable("")
    val marblesBodyObservable = Observable<Marble>()
    val logoutStatusObservable = Observable(true)
    var groupsAndPillsBodyObservable = Observable<Groups>()

    val isLogoutBusyLiveData = MutableLiveData(false)
    val isRetMarbleDataBusyLiveData = MutableLiveData(false)
    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())
    val isRetBranchesAndBillsDataBusyLiveData = MutableLiveData(false)

    fun retMarbleData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {
        if (isRetMarbleDataBusyLiveData.value!!)
            return
        isRetMarbleDataBusyLiveData.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient
                    .getApiInstance(userData.subBaseURL)
                    .getBarcode(
                        schema, barcode, loginCount, employeeNo
                    )

                if (response.isSuccessful) {
                    marblesBodyObservable.setValue(response.body()!!)
                } else {
                    marblesBodyObservable.setValue(
                        Gson().fromJson(
                            response.errorBody()!!.string(),
                            Marble::class.java
                        )
                    )
                }

                alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
            } catch (e: IOException) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, server_is_unreachable)
                println("MainMenuViewModel => retMarbleData() => IOException: " + e.message)
            } catch (e: Exception) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, error_occurred)
                println("MainMenuViewModel => retMarbleData() => Exception: " + e.message)
            }

            isRetMarbleDataBusyLiveData.value = false
        }
    }

    fun logout() {
        if (isLogoutBusyLiveData.value!!)
            return
        isLogoutBusyLiveData.value = true

        viewModelScope.launch {
            try {
                userDao.logout()
                logoutStatusObservable.setValue(true)
            } catch (e: Exception) {
                logoutStatusObservable.setValue(false)
            }
            isLogoutBusyLiveData.value = false
        }
    }

    fun retBranchesAndPills() {
        if (isRetBranchesAndBillsDataBusyLiveData.value!!)
            return

        isRetBranchesAndBillsDataBusyLiveData.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient
                    .getApiInstance(userData.subBaseURL)
                    .getBranches(
                        userData.schema,
                        userData.loginCount,
                        userData.employeeNumber
                    )

                if (response.isSuccessful) {
                    groupsAndPillsBodyObservable.setValue(response.body())
                } else {
                    groupsAndPillsBodyObservable.setValue(
                        Gson().fromJson(
                            response.errorBody()!!.string(),
                            Groups::class.java
                        )
                    )
                }
                alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
            } catch (e: IOException) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, server_is_unreachable)
                println("MainMenuViewModel => retBranchesAndPills() => IOException: " + e.message)
            } catch (e: Exception) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, error_occurred)
                println("MainMenuViewModel => retBranchesAndPills() => Exception: " + e.message)
            }

            isRetBranchesAndBillsDataBusyLiveData.value = false
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