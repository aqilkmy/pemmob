package com.pemmob.mbkmybacaankomik.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton konfigurasi Retrofit dan OkHttpClient untuk MangaDex REST API.
 */
object RetrofitInstance {

    private const val BASE_URL = "https://api.mangadex.org/"

    /**
     * Interceptor untuk menambahkan User-Agent resmi.
     * MangaDex mewajibkan User-Agent yang jelas agar tidak dibatasi oleh proteksi Cloudflare.
     */
    private val userAgentInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", "MBKMyBacaanKomik/1.0 (Android; contact@mbkmybacaankomik.app)")
            .header("Accept", "application/json")
            .build()
        chain.proceed(requestWithUserAgent)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dns(MangaDexDns())
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: MangaDexApiService by lazy {
        retrofit.create(MangaDexApiService::class.java)
    }
}
