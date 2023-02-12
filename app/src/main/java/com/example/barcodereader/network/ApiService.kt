package com.example.barcodereader.network

import com.example.barcodereader.network.properties.get.brances.Groups
import com.example.barcodereader.network.properties.get.marble.Marble
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
    ): Response<Marble>

    @GET("branch")
    suspend fun getBranches(
        @Query("schema") schema: String
    ): Response<Groups>

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}


