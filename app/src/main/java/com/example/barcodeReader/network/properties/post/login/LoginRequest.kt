package com.example.barcodeReader.network.properties.post.login


data class LoginRequest(
    val username: String,
    val password: String,
    val schema: String
)
