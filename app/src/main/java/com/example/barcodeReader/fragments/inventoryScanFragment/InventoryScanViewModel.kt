package com.example.barcodeReader.fragments.inventoryScanFragment


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodeReader.EnPack.error
import com.example.barcodeReader.EnPack.error_occurred
import com.example.barcodeReader.EnPack.server_is_unreachable
import com.example.barcodeReader.database.InventoryItem
import com.example.barcodeReader.database.InventoryItemDao
import com.example.barcodeReader.network.RetrofitClient
import com.example.barcodeReader.network.properties.get.marble.Marble
import com.example.barcodeReader.network.properties.post.saveData.SaveDataRequest
import com.example.barcodeReader.network.properties.post.saveData.SaveDataResponse
import com.example.barcodeReader.network.properties.post.saveData.SavedItems
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.AlertDialogErrorMessage
import com.example.barcodeReader.utils.CustomList
import com.example.barcodeReader.utils.Observable
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class InventoryScanViewModel(
    private val inventoryItemDao: InventoryItemDao
) : ViewModel() {
    val isSendDataBusy = MutableLiveData(false)
    val isUpdatingDbBusy = MutableLiveData(false)
    val isRetMarbleDataBusy = MutableLiveData(false)
    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())

    val barcode = Observable("")
    val marblesBody = Observable<Marble>()
    val sentDataResponse = Observable<SaveDataResponse>()

    fun retMarbleData(schema: String, barcode: String, loginCount: Int, employeeNo: String) {
        if (isRetMarbleDataBusy.value!!)
            return
        isRetMarbleDataBusy.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient
                    .getApiInstance(userData.subBaseURL)
                    .getBarcode(
                        schema, barcode, loginCount, employeeNo
                    )

                if (response.isSuccessful) {
                    marblesBody.setValue(response.body()!!)
                } else {
                    marblesBody.setValue(
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
                println("InventoryScanViewModel => retMarbleData() => IOException: " + e.message)
            } catch (e: Exception) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, error_occurred)
                println("InventoryScanViewModel => retMarbleData() => Exception: " + e.message)
            }

            isRetMarbleDataBusy.value = false
        }
    }

    fun updateDB(itemsList: CustomList<InventoryItem>, groupCode: String, pillCode: String) {
        if (isUpdatingDbBusy.value!!)
            return
        isUpdatingDbBusy.value = true

        viewModelScope.launch(Dispatchers.IO) {
            inventoryItemDao.deleteItemsData(groupCode, pillCode, userData.employeeNumber)
            inventoryItemDao.insertItems(itemsList)
            withContext(Dispatchers.Main) {
                isUpdatingDbBusy.value = false
            }
        }
    }

    suspend fun retData(groupCode: String, pillCode: String) =
        inventoryItemDao.retItemsSuspend(groupCode, pillCode, userData.employeeNumber)

    fun sendData(
        itemsList: CustomList<InventoryItem>,
        schema: String,
        pillCode: String,
        pillName: String,
        employeeNumber: String,
        groupCode: String,
        groupMgr: String,
        date: String
    ) {
        if (isSendDataBusy.value!!)
            return
        isSendDataBusy.value = true

        viewModelScope.launch {
            try {
                val convertedList = mutableListOf<SavedItems>()
                itemsList.forEach {
                    convertedList.add(
                        SavedItems(
                            groupCode,
                            it.frz,
                            it.itemCode,
                            it.number,
                            it.amount,
                            it.unitCode,
                            it.length,
                            it.width,
                            it.height,
                            date
                        )
                    )
                }

                val saveDataRequest = SaveDataRequest(
                    groupMgr,
                    employeeNumber,
                    groupCode,
                    pillName,
                    pillCode,
                    convertedList,
                    schema
                )

                val response = RetrofitClient
                    .getApiInstance(userData.subBaseURL)
                    .sendData(
                        saveDataRequest,
                        userData.loginCount,
                        userData.employeeNumber
                    )

                if (response.isSuccessful) {
                    sentDataResponse.setValue(
                        response.body()
                    )
                } else {
                    sentDataResponse.setValue(
                        Gson().fromJson(
                            response.errorBody()!!.string(),
                            SaveDataResponse::class.java
                        )
                    )
                }

                alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
            } catch (e: IOException) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, server_is_unreachable)
                println("InventoryScanViewModel => retMarbleData() => IOException: " + e.message)
            } catch (e: Exception) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, error, error_occurred)
                println("InventoryScanViewModel => retMarbleData() => Exception: " + e.message)
            }

            isSendDataBusy.value = false
        }
    }

    class InventoryScanViewModelFactory(
        private val inventoryItemDao: InventoryItemDao
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryScanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventoryScanViewModel(inventoryItemDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
