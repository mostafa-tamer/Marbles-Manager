package com.example.barcodereader.network.properties.post.login


data class LoginRequest(
    val userName: String,
    val password: String,
    val schema: String
)
