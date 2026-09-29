package com.example.data.remote

import com.example.config.TmdbConfig
import com.squareup.moshi.Moshi
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object TmdbNetworkClient {

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val key = TmdbConfig.apiKey

        val newRequest = if (TmdbConfig.isBearerToken) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .build()
        } else {
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter("api_key", key)
                .build()
            originalRequest.newBuilder()
                .url(newUrl)
                .addHeader("Accept", "application/json")
                .build()
        }
        chain.proceed(newRequest)
    }

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val moshi: Moshi by lazy {
        TmdbApiService.createMoshi()
    }

    val apiService: TmdbApiService by lazy {
        TmdbApiService.create(
            okHttpClient = httpClient,
            moshi = moshi,
            baseUrl = TmdbConfig.API_BASE_URL
        )
    }
}
