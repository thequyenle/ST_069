package net.android.st069_fakecallphoneprank.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Your actual server URL
    private const val BASE_URL = "https://lvtglobal.site/"

    // Public base URL for constructing full image/mp3 paths
    const val MEDIA_BASE_URL = BASE_URL

    // Lazy initialization
    private val retrofit: Retrofit by lazy {
        // Logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttp client
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Retrofit instance
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Service instance
    val fakeCallApiService: FakeCallApiService by lazy {
        retrofit.create(FakeCallApiService::class.java)
    }
}