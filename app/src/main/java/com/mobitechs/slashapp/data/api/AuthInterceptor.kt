package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.local.SharedPrefsManager
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(
    private val sharedPrefsManager: SharedPrefsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get JWT token from shared preferences
        val token = sharedPrefsManager.getAuthToken()


        // If token is not available, proceed with the original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Add token to request header
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}