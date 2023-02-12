package com.example.barcodereader.network.properties.post


data class LoginRequest(
    val userName: String,
    val password: String,
    val schema: String
)
