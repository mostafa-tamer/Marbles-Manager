package com.example.barcodeReader.network.properties.post.saveData

data class SaveDataResponse(
    val timeStamp: String,
    val statusCode: Int,
    val message: String
)