package com.mobitechs.slashapp.data.api

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import  com.mobitechs.slashapp.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

    private const val TIMEOUT = 30L

    // Create OkHttpClient with security features
    private fun createOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        // Certificate pinning for added security
        val certificatePinner = CertificatePinner.Builder()
            .add(Constants.API_DOMAIN, Constants.CERTIFICATE_PIN)
            .build()

        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
//            .certificatePinner(certificatePinner)
            .addInterceptor(authInterceptor)


        // Add logging interceptor only in debug builds
//        if (BuildConfig.DEBUG) {
//            val loggingInterceptor = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//            builder.addInterceptor(loggingInterceptor)
//        }

        return builder.build()
    }

    // Create Retrofit instance
    fun getRetrofitInstance(authInterceptor: AuthInterceptor): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(createOkHttpClient(authInterceptor))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}