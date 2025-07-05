package com.mobitechs.slashapp.data.model

class AuthResponse {
}

data class SendOTPResponse(
    val success: Boolean,
    val message: String,
    val data: SendOTPDetailsResponse,
)


data class SendOTPDetailsResponse(
    val otp: String,
    val expires_in: String
)