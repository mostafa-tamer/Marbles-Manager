package com.example.barcodereader.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Api(subBaseURL: String) {

    private val url = "http://$subBaseURL/api/v1/topsoftware/"
//    private val url = "http://192.168.191.142:8085/api/v1/topsoftware/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(OkHttpClient.Builder().build())
        .build()

    val call = retrofit.create(ApiService::class.java)
}