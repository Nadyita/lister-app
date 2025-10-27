package xyz.travitia.lister.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "ListerAPI"
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null
    private var currentBearerToken: String? = null

    fun getApiService(baseUrl: String, bearerToken: String? = null): ListerApiService {
        if (retrofit == null || currentBaseUrl != baseUrl || currentBearerToken != bearerToken) {
            currentBaseUrl = baseUrl
            currentBearerToken = bearerToken

            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                if (!bearerToken.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $bearerToken")
                    Log.d(TAG, "Adding Bearer token to request")
                }
                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .serializeNulls()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            Log.d(TAG, "===== API Client initialized with Base URL: $baseUrl =====")
            Log.d(TAG, "===== Bearer token: ${if (bearerToken.isNullOrBlank()) "None" else "Set"} =====")
        }

        return retrofit!!.create(ListerApiService::class.java)
    }
}

