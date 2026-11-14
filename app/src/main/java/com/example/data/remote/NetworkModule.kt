package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    private const val MODRINTH_BASE_URL = "https://api.modrinth.com/"

    private val userAgentInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .header("User-Agent", "MichichModsHub/1.0 (android; contact@michichhub.app)")
            .build()
        chain.proceed(req)
    }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MODRINTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val modrinthApi: ModrinthApi by lazy { retrofit.create(ModrinthApi::class.java) }
    val mojangApi: MojangApi by lazy { retrofit.create(MojangApi::class.java) }
}
