package com.example.barcodeReader.fragments.offlineModeFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.database.InventoryItemOfflineModeDao
import com.example.barcodeReader.utils.AlertDialogErrorMessage

class OfflineModeViewModel(private val inventoryItemOfflineModeDao: InventoryItemOfflineModeDao) :
    ViewModel() {

    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())

    suspend fun saveDataDB(inventoryItems: List<InventoryItemOfflineMode>) {
        inventoryItemOfflineModeDao.insertItems(inventoryItems)
    }

    suspend fun deleteDataDB() {
        inventoryItemOfflineModeDao.deleteItemsData()
    }

    fun retDataDB() =
        inventoryItemOfflineModeDao.retItems()

    class OfflineModeViewModelFactory(private val inventoryItemOfflineModeDao: InventoryItemOfflineModeDao) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OfflineModeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OfflineModeViewModel(inventoryItemOfflineModeDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}