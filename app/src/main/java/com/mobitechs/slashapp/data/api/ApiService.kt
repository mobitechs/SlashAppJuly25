package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.ApiResponse
import com.mobitechs.slashapp.data.model.OTPVerifyResponse
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.model.SendOTPResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Auth endpoints
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOTPResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<OTPVerifyResponse>


   @POST("auth/register")
    suspend fun register(@Body request: RegisterUserRequest): Response<OTPVerifyResponse>


}