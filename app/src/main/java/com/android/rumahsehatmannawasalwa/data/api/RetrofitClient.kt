package com.android.rumahsehatmannawasalwa.data.api

import android.util.Log
import com.android.rumahsehatmannawasalwa.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    public const val BASE_URL = BuildConfig.BASE_URL

    @Volatile
    var authToken: String? = null

    private val client by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                requestBuilder.addHeader("Accept", "application/json")

                // Gunakan token yang diset dari AuthRepository (Sanctum Token)
                authToken?.let {
                    Log.d(TAG, "Mengirim request dengan Sanctum Token...")
                    requestBuilder.header("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
            }
            .authenticator { _, response ->
                // Jika 401, jangan refresh via Firebase di sini.
                // Karena Sanctum token biasanya long-lived.
                // Jika 401, berarti user harus login ulang.
                if (response.code == 401) {
                    Log.e(TAG, "Sanctum Token Expired atau Tidak Valid. Menghentikan request.")
                    authToken = null
                }
                null
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}