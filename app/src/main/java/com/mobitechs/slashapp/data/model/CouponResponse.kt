package com.mobitechs.slashapp.data.model


data class CouponListResponse(
    val success: Boolean,
    val data: List<CouponListItem>
)

data class CouponResponse(
    val success: Boolean,
    val data: CouponListItem
)


data class CouponListItem(
    val id: Int,
    val code: String,
    val title: String,
    val description: String?,
    val discount_amount: String,
    val discount_percentage: String?,
    val store_id: Int?,
    val min_order_amount: String,
    val max_discount: String?,
    val valid_from: String,
    val valid_until: String,
    val usage_limit: Int?,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)