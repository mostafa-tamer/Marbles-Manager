package com.example.barcodereader.network

import com.example.barcodereader.network.properties.get.Statistics
import com.example.barcodereader.network.properties.post.LoginRequest
import com.example.barcodereader.network.properties.post.LoginResponse
import retrofit2.Response
import retrofit2.http.*


interface ApiService {
    @GET("barcode")
    suspend fun getBarcode(
        @Query("schema") schema: String,
        @Query("barcode") barcode: String,
        @Query("loginCount") loginCount: Int,
        @Query("employeeNo") employeeNumber: String
    ): Response<Statistics>

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}


