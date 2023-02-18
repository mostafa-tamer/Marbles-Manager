
package com.example.barcodereader.fragments.inventoryScanFragment

import AESEncryption
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.InventoryItem
import com.example.barcodereader.databaes.InventoryItemDao
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.fragments.viewModels.ScanViewModel
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.post.saveData.SaveDataRequest
import com.example.barcodereader.network.properties.post.saveData.SaveDataResponse
import com.example.barcodereader.network.properties.post.saveData.SavedItems
import com.example.barcodereader.userData
import com.example.barcodereader.utils.CustomList
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import kotlinx.coroutines.launch
import retrofit2.Response

class InventoryScanViewModel(
    private val userDao: UserDao,
    private val inventoryItemDao: InventoryItemDao
) : ScanViewModel(userDao) {

    val sentDataResponse = Observable<Response<SaveDataResponse>>()

    fun saveDataDB(inventoryItems: List<InventoryItem>) {
        viewModelScope.launch {
            inventoryItemDao.insertItems(inventoryItems)
        }
    }

    fun deleteDataDB(groupCode: String, pillCode: String) {
        viewModelScope.launch {
            inventoryItemDao.deleteItemsData(groupCode, pillCode, userData.employeeNumber)
        }
    }

    fun retData(groupCode: String, pillCode: String) =
        inventoryItemDao.retItems(groupCode, pillCode, userData.employeeNumber)

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
                    AESEncryption.decrypt(schema, GlobalKeys.KEY)
                )

                sentDataResponse.setValue(
                    api.call.sendData(
                        saveDataRequest,
                        userData.loginCount,
                        userData.employeeNumber
                    )
                )

                connectionStatus.setValue(true)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
                println(e.message)
            }
        }
    }


    class InventoryScanViewModelFactory(
        private val userDao: UserDao,
        private val inventoryItemDao: InventoryItemDao
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryScanViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventoryScanViewModel(userDao, inventoryItemDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
