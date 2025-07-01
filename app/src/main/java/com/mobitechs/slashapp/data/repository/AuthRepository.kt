package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    fun getCurrentUserId(): String? {
        val user = sharedPrefsManager.getUser()
        return user?.id.toString()
    }

    suspend fun sendOtp(phoneNumber: String): ApiResponse = withContext(Dispatchers.IO) {
        val request = SendOtpRequest(phone = phoneNumber)
        val response = apiService.sendOtp(request)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Send OTP failed: ${response.message()}")
        }
    }

    suspend fun verifyOtp(phoneNumber: String, otp: String): ApiResponse = withContext(Dispatchers.IO) {
        val request = VerifyOtpRequest(phone = phoneNumber, otp = otp)
        val response = apiService.verifyOtp(request)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")

            return@withContext apiResponse
        } else {
            throw Exception("Verify OTP failed: ${response.message()}")
        }
    }

    fun saveAuthData(token: String, user: User) {
        sharedPrefsManager.saveAuthToken(token)
        sharedPrefsManager.saveUser(user)
    }

    fun getCurrentUser(): User? {
        return sharedPrefsManager.getUser()
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefsManager.isLoggedIn()
    }

    fun logout() {
        sharedPrefsManager.logout()
    }
}