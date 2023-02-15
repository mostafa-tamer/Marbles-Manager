package com.example.barcodereader.network.properties.post.saveData

data class SaveDataRequest(
    val empBranCode: String,
    val employeeCode: String,
    val inventoryCode: String,
    val invoiceName: String,
    val invoiceType: String,
    val savedItemsList: List<SavedItems>,
    val schema: String
)