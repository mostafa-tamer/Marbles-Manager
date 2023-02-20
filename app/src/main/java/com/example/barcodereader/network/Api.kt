package com.example.barcodereader.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor() {
    companion object {
        private lateinit var api: ApiService

        @Synchronized
        fun getApiInstance(subBaseURL: String): ApiService {
            return if (!::api.isInitialized) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://$subBaseURL/api/v1/topsoftware/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(
                        OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(600, TimeUnit.SECONDS)
                            .build()
                    ).build()
                retrofit.create(ApiService::class.java)
            } else {
                api
            }
        }
    }
}