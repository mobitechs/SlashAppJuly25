package com.mobitechs.slashapp.data.model

data class StoreReviewsListResponse(
    val data: List<StoreReviewsListItem>,
    val pagination: Pagination,
    val success: Boolean,
    val message: String?
)

data class StoreReviewsListItem(
    val created_at: String,
    val description: String,
    val first_name: String,
    val helpful_count: Int,
    val id: Int,
    val is_active: Int,
    val last_name: String,
    val profile_picture: Any,
    val rating: Int,
    val report_count: Int,
    val store_id: Int,
    val title: String,
    val updated_at: String,
    val user_id: Int,
    val total_helpful_count: Int,
    val total_report_count: Int,
    val is_marked_helpful: Boolean,
    val is_marked_report: Boolean,

)

data class UserReview(
    val created_at: String,
    val description: String,
    val helpful_count: Int,
    val id: Int,
    val is_active: Int,
    val rating: Int,
    val report_count: Int,
    val store_id: Int,
    val title: String,
    val updated_at: String,
    val user_id: Int
)