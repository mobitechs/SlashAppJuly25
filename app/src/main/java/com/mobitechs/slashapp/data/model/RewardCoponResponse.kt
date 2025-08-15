package com.mobitechs.slashapp.data.model

data class RewardCouponResponse(
    val data: RewardCouponData,
    val success: Boolean
)

data class RewardCouponData(
    val available_coupons: List<AvailableCoupon>,
    val summary: Summary,
    val user_info: UserInfo
)

data class AvailableCoupon(
    val applicable_store_name: String,
    val coupon_id: Int,
    val coupon_name: String,
    val created_at: String,
    val description: String,
    val discount_display: String,
    val discount_type: String,
    val discount_value: String,
    val expiry_date: Any,
    val is_percentage: Boolean,
    val lifetime_validity: Int,
    val store_id: Any,
    val updated_at: String
)

data class Summary(
    val available_count: Int
)


data class UserInfo(
    val is_new_user: Boolean,
    val is_vip: Boolean
)