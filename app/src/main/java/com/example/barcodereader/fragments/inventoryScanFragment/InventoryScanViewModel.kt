package com.example.barcodereader.fragments.inventoryScanFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.post.saveData.SaveDataRequest
import com.example.barcodereader.network.properties.post.saveData.SaveDataResponse
import com.example.barcodereader.network.properties.post.saveData.SavedItems
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CustomList
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.launch
import retrofit2.Response

class InventoryScanViewModel(private val userDao: UserDao) : ScanViewModel(userDao) {

    val saveDataResponse = Observable<Response<SaveDataResponse>>()

    fun saveData(
        itemsList: CustomList<InventoryItem>,
        schema: String,
        pillCode: String,
        pillName: String,
        employeeNumber: String,
        groupCode: String,
        groupMgr: String,
        date: String
    ) {
        viewModelScope.launch {
            try {
                val api = Api(userData.subBaseURL)

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

                saveDataResponse.setValue(
                    api.call.saveData(
                        saveDataRequest
                    )
                )


            } catch (e: Exception) {
                saveDataResponse.setValue(null)
                e.stackTrace.forEach {
                    println(it)
                }
            }
        }
    }


    class InventoryScanViewModelFactory(
        private val userDao: UserDao
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryScanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventoryScanViewModel(userDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}