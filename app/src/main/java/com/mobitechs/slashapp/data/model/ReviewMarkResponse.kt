package com.mobitechs.slashapp.data.model

data class ReviewMarkResponse(
    val data: ReviewMarkResponseDate,
    val message: String,
    val success: Boolean
)

data class ReviewMarkResponseDate(
    val is_marked: Boolean,
    val review_id: Int
)