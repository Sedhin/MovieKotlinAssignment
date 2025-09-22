package com.exam.samplemoviekotlin

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("movies")
    suspend fun getMovies(): Response<List<ApiMovieResponse>>

    companion object {
        private const val BASE_URL = "https://68cc08ab716562cf507620db.mockapi.io/"

        fun create(cacheDir: File): ApiService {
            val cache = Cache(
                directory = File(cacheDir, "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MB cache
            )

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Reduced logging
            }

            val client = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}