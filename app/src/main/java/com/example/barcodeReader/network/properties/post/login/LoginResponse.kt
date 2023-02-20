package com.example.barcodeReader.network.properties.post.login

data class LoginResponse(
    val timeStamp: String,
    val statusCode: Int,
    val message: String,
    val data: Data
)
