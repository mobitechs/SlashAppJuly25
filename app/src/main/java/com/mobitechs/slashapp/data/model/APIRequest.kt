package com.mobitechs.slashapp.data.model

data class SendOtpRequest(
    val phone_number: String
)

data class VerifyOtpRequest(
    val phone_number: String,
    val otp: String
)


data class RegisterUserRequest(
    val phone_number: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val referral_code: String
)