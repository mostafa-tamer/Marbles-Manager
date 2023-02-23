package com.example.barcodeReader.fragments.offlineModeFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
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
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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

    private fun cellStyling(
        workbook: XSSFWorkbook,
        indexedColors: Short,
        fillPatternType: FillPatternType,
        horizontalAlignment: HorizontalAlignment
    ): XSSFCellStyle {

        val cellStyle: XSSFCellStyle = workbook.createCellStyle()
        cellStyle.fillForegroundColor = indexedColors
        cellStyle.fillPattern = fillPatternType
        cellStyle.alignment = horizontalAlignment
        return cellStyle
    }

    private fun createCell(
        cellRow: XSSFRow,
        cellIndex: Int,
        cellText: String,
        cellStyle: XSSFCellStyle
    ) {
        val cell = cellRow.createCell(cellIndex)
        cell.setCellValue(cellText)
        cell.cellStyle = cellStyle
    }

    private fun createCell(
        cellRow: XSSFRow,
        cellIndex: Int,
        cellText: Double,
        cellStyle: XSSFCellStyle
    ) {
        val cell = cellRow.createCell(cellIndex)
        cell.setCellValue(cellText)
        cell.cellStyle = cellStyle
    }

    fun exportExcelSheet(
        itemsList: CustomList<InventoryItemOfflineMode>, context: Context
    ) {
        if (isSavingDataBusy.value!!)
            return
        isSavingDataBusy.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Sheet1")

                val headerCellStyle = cellStyling(
                    workbook,
                    IndexedColors.AQUA.index,
                    FillPatternType.SOLID_FOREGROUND,
                    HorizontalAlignment.CENTER
                )

                val mainRow = sheet.createRow(0)

                createCell(mainRow, 0, "Code", headerCellStyle)
                createCell(mainRow, 1, "Number", headerCellStyle)

                for (i in 0 until itemsList.size) {

                    val rowsCellStyle = cellStyling(
                        workbook,
                        IndexedColors.YELLOW.index,
                        FillPatternType.SOLID_FOREGROUND,
                        HorizontalAlignment.CENTER,
                    )

                    val row = sheet.createRow(i + 1)

                    createCell(row, 0, itemsList[i].itemCode, rowsCellStyle)
                    createCell(row, 1, itemsList[i].number.toDouble(), rowsCellStyle)
                }

                saveToAppAbsolutePathAndShare(context, workbook)
                saveToDocuments(workbook)


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

    private fun saveToAppAbsolutePathAndShare(
        context: Context,
        workbook: XSSFWorkbook
    ) {
        val filePathDir: String =
            context.getExternalFilesDir(null)?.absolutePath + "/Items.xlsx"
        val fileOutDir = FileOutputStream(filePathDir)
        workbook.write(fileOutDir)
        fileOutDir.close()
        val file = File(filePathDir)
        shareFile(context, file)
    }

    private fun saveToDocuments(workbook: XSSFWorkbook) {
        val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy HH%mm%ss")
        val calendar = Calendar.getInstance()
        val timeNow = dateTimeFormat.format(calendar.time)
        val fileName = "Items $timeNow.xlsx"

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        if (!downloadsDir!!.exists()) {
            downloadsDir.mkdirs()
        }

        val filePath = File(downloadsDir, fileName)
        val fileOut = FileOutputStream(filePath)
        workbook.write(fileOut)
        fileOut.close()
    }

    fun test() {
        // Replace outputFilePath with the actual path where you want to save the file
        val outputFilePath = "/path/to/output/file.xlsx"

// Create a new XSSFWorkbook
        val workbook = XSSFWorkbook()

// Create a new sheet in the workbook
        val sheet = workbook.createSheet("Sheet1")

// Create a header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("Age")
        headerRow.createCell(2).setCellValue("Email")

// Create data rows
        val data = listOf(
            listOf("Alice", 30, "alice@example.com"),
            listOf("Bob", 25, "bob@example.com"),
            listOf("Charlie", 35, "charlie@example.com")
        )

        for ((i, row) in data.withIndex()) {
            val dataRow = sheet.createRow(i + 1)
            dataRow.createCell(0).setCellValue(row[0] as String)
            dataRow.createCell(2).setCellValue(row[2] as String)
        }

// Write the workbook to the output file
        FileOutputStream(outputFilePath).use { outputStream ->
            workbook.write(outputStream)
        }

// Close the workbook
        workbook.close()
    }

    private fun shareFile(context: Context, file: File) {
//        val mimeTypeMap = MimeTypeMap.getSingleton()
//        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
//        var type = mimeTypeMap.getExtensionFromMimeType(ext)
//
//        if (type == null) type = "*/*"

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