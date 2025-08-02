package com.mobitechs.slashapp.data.model

data class StoreListResponse(
    val success: Boolean,
    val pagination: Pagination,
    val data: List<StoreListItem>
)

data class StoreResponse(
    val success: Boolean,
    val data: StoreListItem

)

data class StoreListItem(
    val id: Int,
    val name: String,
    val category_id: Int,
    val category_name: String,
    val description: String?,
    val phone_number: String?,
    val email: String?,
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val logo: String?,
    val banner_image: String?,
    val rating: String,
    val total_reviews: String,
    val normal_discount_percentage: String,
    val vip_discount_percentage: String,
    val minimum_order_amount: String,
    val qr_code: String?,
    val upi_id: String?,
    val google_business_url: String?,
    val is_partner: Int,
    val is_active: Int,
    val created_at: String,
    val updated_at: String,
    val review_count: Int?,
    val transaction_count: Int?
)


data class AddStoreReviewResponse(
    val success: Boolean,
    val message: String,
    val data: AddStoreReviewResponseId,
)

data class AddStoreReviewResponseId(
    val id: Int
)

