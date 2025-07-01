package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.ApiResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Auth endpoints
    @POST("student/login")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse>

    @POST("student/register")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse>


}