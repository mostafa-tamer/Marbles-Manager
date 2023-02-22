package com.example.barcodeReader.fragments.offlineModeFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.*


class OfflineModeViewModel(private val inventoryItemOfflineModeDao: InventoryItemOfflineModeDao) :
    ViewModel() {

    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())
    val isSavingDataBusy = MutableLiveData(false)
    val isUpdatingDbBusy = MutableLiveData(false)


    fun updateDB(itemsList: CustomList<InventoryItemOfflineMode>) {
        if (isUpdatingDbBusy.value!!)
            return
        isUpdatingDbBusy.value = true
        viewModelScope.launch(Dispatchers.IO) {
            inventoryItemOfflineModeDao.deleteItemsData()
            inventoryItemOfflineModeDao.insertItems(itemsList)
            withContext(Dispatchers.Main) {
                isUpdatingDbBusy.value = false
            }
        }
    }

    suspend fun retDataDB() =
        inventoryItemOfflineModeDao.retItemsSuspend()

    fun exportExcelSheet(
        itemsList: CustomList<InventoryItemOfflineMode>, context: Context
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
                cell.setCellValue("Code")
                cell.setCellStyle(cellStyle)

                cell = mainRow.createCell(1)
                cell.setCellValue("Number")
                cell.setCellStyle(cellStyle)


                for (i in 0 until itemsList.size) {
                    val cellStyle: CellStyle = workbook.createCellStyle().apply {
                        fillForegroundColor = HSSFColor.YELLOW.index
                        fillPattern = HSSFCellStyle.SOLID_FOREGROUND
                        alignment = CellStyle.ALIGN_CENTER
                    }

                    val row = sheet.createRow(i + 1)

                    cell = row.createCell(0)
                    cell.setCellValue(itemsList[i].itemCode)
                    cell.setCellStyle(cellStyle)

                    cell = row.createCell(1)
                    cell.setCellValue(itemsList[i].number.toDouble())
                    cell.setCellStyle(cellStyle)

                }

                val filePathDir: String =
                    context.getExternalFilesDir(null)?.absolutePath + "/Items.xls"
                val fileOutDir = FileOutputStream(filePathDir)
                workbook.write(fileOutDir)
                fileOutDir.close()
                val file = File(filePathDir)
                shareFile(context, file)

                val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH;mm;ss")
                val calendar = Calendar.getInstance()
                val timeNow = dateTimeFormat.format(calendar.time)
                val fileName = "Items $timeNow.xls"

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

                if (!downloadsDir!!.exists()) {
                    downloadsDir.mkdirs()
                }

                val filePath = File(downloadsDir, fileName)
                val fileOut = FileOutputStream(filePath)
                workbook.write(fileOut)
                fileOut.close()



                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Item saved to documents", Toast.LENGTH_SHORT).show()
                    alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    alertDialogErrorMessageLiveData.value =
                        AlertDialogErrorMessage(true, "Error", "Error on saving")
                }
                println("OfflineModeViewModel => exportExcelSheet(): " + e.message)
            }
            withContext(Dispatchers.Main) {
                isSavingDataBusy.value = false
            }
        }
    }



    private fun shareFile(context: Context, file: File) {
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