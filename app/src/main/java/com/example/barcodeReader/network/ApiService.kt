package com.example.barcodeReader.network

import com.example.barcodeReader.network.properties.get.groups.Groups
import com.example.barcodeReader.network.properties.get.marble.Marble
import com.example.barcodeReader.network.properties.post.login.LoginRequest
import com.example.barcodeReader.network.properties.post.login.LoginResponse
import com.example.barcodeReader.network.properties.post.saveData.SaveDataRequest
import com.example.barcodeReader.network.properties.post.saveData.SaveDataResponse
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

    @GET("inventory")
    suspend fun getBranches(
        @Query("schema") schema: String,
        @Query("loginCount") loginCount: Int,
        @Query("employeeNo") employeeNumber: String
    ): Response<Groups>

    @POST("login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body loginRequest: LoginRequest,
    ): Response<LoginResponse>

    @POST("save")
    @Headers("Content-Type: application/json")
    suspend fun sendData(
        @Body loginRequest: SaveDataRequest,
        @Query("loginCount") loginCount: Int,
        @Query("employeeNo") employeeNumber: String
    ): Response<SaveDataResponse>
}


