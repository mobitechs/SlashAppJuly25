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

data class ValidateCouponRequest(
    val code: String,
    val store_id: Int,
    val bill_amount: Double
)

data class CreateTransactionRequest(
    val user_id: Int,
    val store_id: Int,
    val bill_amount: Double,
    val vendor_discount: Double,
    val cashback_used: Double,
    val coupon_code: String?,
    val coupon_discount: Double,
    val final_amount: Double,
    val payment_method: String,
    val comment: String?
)