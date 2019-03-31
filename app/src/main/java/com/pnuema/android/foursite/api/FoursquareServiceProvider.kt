package com.pnuema.android.foursite.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Helper class to provide a mechanism to lazy load Retrofit and provide the Foursquare service when requests needed.
 */
class FoursquareServiceProvider {
    companion object {
        private const val CONNECT_TIMEOUT: Long = 15
        private const val READ_TIMEOUT: Long = 15
        private const val WRITE_TIMEOUT: Long = 15
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build()
        }
        val service: FoursquareService by lazy {
            Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .baseUrl("https://api.foursquare.com")
                .build()
                .create(FoursquareService::class.java)
        }
    }
}

