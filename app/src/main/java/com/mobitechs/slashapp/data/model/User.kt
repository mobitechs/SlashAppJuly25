package com.mobitechs.slashapp.data.model

data class ProfileResponse(
    val data: User,
    val message: String,
    val success: Boolean
)

data class User(
    val address_building: Any,
    val address_city: Any,
    val address_pincode: Any,
    val address_state: Any,
    val address_street: Any,
    val anniversary_date: Any,
    val available_cashback: String,
    val created_at: String,
    val date_of_birth: Any,
    val email: String,
    val favorite_stores_count: Int,
    val first_name: String,
    val gender: Any,
    val id: Int,
    val is_email_verified: Int,
    val is_phone_verified: Int,
    val last_name: String,
    val phone_number: String,
    val profile_completion_percentage: Int,
    val profile_picture: Any,
    val referral_code: String,
    val spouse_birth_date: Any,
    val total_cashback_earned: String,
    val total_cashback_redeemed: String,
    val total_coupon_redeemed: String,
    val total_referrals: Int,
    val total_transactions: Int,
    val wallet: Wallet
)

data class Wallet(
    val available_cashback: String,
    val total_earned: String
)


