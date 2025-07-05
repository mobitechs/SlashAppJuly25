package com.mobitechs.slashapp.data.model



data class User(
    val email: String?,
    val first_name: String?,
    val id: Int,
    val last_name: String?,
    val phone_number: String,
    var profile_completion_percentage: Int?,
    val referral_code: String?,
    val wallet: Wallet?
)


data class Wallet(
    val available_cashback: String,
    val total_earned: String
)




data class ApiResponse(
    val status_code: Int,
    val message: String,
    val data: Any? = null
)