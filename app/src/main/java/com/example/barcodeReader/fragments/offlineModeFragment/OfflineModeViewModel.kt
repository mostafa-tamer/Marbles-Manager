package com.example.barcodeReader.fragments.offlineModeFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodeReader.database.InventoryItemOfflineMode
import com.example.barcodeReader.database.InventoryItemOfflineModeDao
import com.example.barcodeReader.utils.AlertDialogErrorMessage
import com.example.barcodeReader.utils.CustomList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import java.io.File
import java.io.FileOutputStream


class OfflineModeViewModel(private val inventoryItemOfflineModeDao: InventoryItemOfflineModeDao) :
    ViewModel() {

    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())

    val isSavingDataBusy = MutableLiveData(false)

    fun updateDB(itemsList: CustomList<InventoryItemOfflineMode>) {
        viewModelScope.launch(Dispatchers.IO) {
            inventoryItemOfflineModeDao.deleteItemsData()
            for (i in itemsList) {
                inventoryItemOfflineModeDao.insertItems(i)
            }
        }
    }

    fun retDataDB() =
        inventoryItemOfflineModeDao.retItems()

    fun exportExcelSheet(
        context: Context,
        itemsList: CustomList<InventoryItemOfflineMode>
    ) {
        if (isSavingDataBusy.value!!)
            return
        isSavingDataBusy.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workbook = HSSFWorkbook()
                val sheet = workbook.createSheet("Sheet1")

                val cellStyle: CellStyle = workbook.createCellStyle().apply {
                    fillForegroundColor = HSSFColor.AQUA.index
                    fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                    alignment = CellStyle.ALIGN_CENTER
                }

                lateinit var cell: Cell

                val mainRow = sheet.createRow(0)

                cell = mainRow.createCell(0)
                cell.setCellValue("Item Code")
                cell.setCellStyle(cellStyle)

                cell = mainRow.createCell(1)
                cell.setCellValue("Amount")
                cell.setCellStyle(cellStyle)

                cell = mainRow.createCell(2)
                cell.setCellValue("Number")
                cell.setCellStyle(cellStyle)

                for (i in 1 until itemsList.size) {
                    val cellStyle: CellStyle = workbook.createCellStyle().apply {
                        fillForegroundColor = HSSFColor.YELLOW.index
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        alignment = CellStyle.ALIGN_CENTER
                    }

                    val row = sheet.createRow(i)

                    cell = row.createCell(0)
                    cell.setCellValue(itemsList[i].itemCode)
                    cell.setCellStyle(cellStyle)

                    cell = row.createCell(1)
                    cell.setCellValue(itemsList[i].amount)
                    cell.setCellStyle(cellStyle)

                    cell = row.createCell(2)
                    cell.setCellValue(itemsList[i].number)
                    cell.setCellStyle(cellStyle)
                }

                val filePath = context.getExternalFilesDir(null)?.absolutePath + "/Items.xls"
                val fileOut = FileOutputStream(filePath)
                workbook.write(fileOut)
                fileOut.close()

                val file = File(filePath)

                val mimeTypeMap = MimeTypeMap.getSingleton()
                val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
                var type = mimeTypeMap.getExtensionFromMimeType(ext)

                if (type == null) type = "*/*"


                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, "Sharing File from File Downloader")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val path: Uri = FileProvider.getUriForFile(
                        context,
                        "com.example.barcodeReader",
                        file
                    )
                    intent.putExtra(Intent.EXTRA_STREAM, path)
                } else {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                }
                intent.type = "*/*"
                context.startActivity(Intent.createChooser(intent, "Share File"))
                withContext(Dispatchers.Main) {
                    alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    alertDialogErrorMessageLiveData.value =
                        AlertDialogErrorMessage(true, "Error", "Error on saving")
                }
                println("OfflineModeViewModel => exportExcelSheet()" + e.message)
            }
            withContext(Dispatchers.Main) {
                isSavingDataBusy.value = false
            }
        }
    }


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