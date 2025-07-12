package com.mobitechs.slashapp.data.model


data class CouponListResponse(
    val success: Boolean,
    val message: String,
    val count: Int,
    val data: List<CouponListItem>
)


data class CouponResponse(
    val success: Boolean,
    val message: String,
    val data: CouponListItem,

)

data class CouponValidationResponse(
    val success: Boolean,
    val message: String,
    val data: CouponValidationData
)

data class CouponValidationData(
    val coupon: CouponListItem,
    val discount_details: CouponDiscountDetails
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
    val updated_at: String,
    val discount_details: CouponDiscountDetails
)

data class CouponDiscountDetails(
    val original_amount: String,
    val discount_amount: String,
    val final_amount: String,
    val discount_type: String,
    val savings_percentage: String
)