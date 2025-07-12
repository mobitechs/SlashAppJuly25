package com.mobitechs.slashapp.data.model

data class OTPVerifyResponse(
    val data: OTPVerifyData,
    val message: String,
    val success: Boolean,
    val errors: List<Error>?,
)


data class OTPVerifyData(
    val is_new_user: Boolean,
    val token: String,
    val user: User
)



data class Error(
    val field: String,
    val message: String
)

