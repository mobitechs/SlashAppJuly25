package com.mobitechs.slashapp.data.model

data class SpinWheelHistoryResponse(
    val data: SpinWheelHistoryData,
    val success: Boolean
)

data class SpinWheelHistoryData(
    val history: List<SpinWheelHistoryItem>,
    val pagination: Pagination
)

data class SpinWheelHistoryItem(
    val campaign_title: String?,
    val color: String?,
    val coupon_name: String?,
    val date: String?,
    val id: Int,
    val is_winner: Boolean,
    val reward_text: String?,
    val reward_type: String?,
    val reward_value: Int
)

//data class SpinWheelHistoryItem(
//    val campaign_title: String,
//    val color: String,
//    val coupon_name: String,
//    val date: String,
//    val id: Int,
//    val is_winner: Boolean,
//    val reward_text: String,
//    val reward_type: String,
//    val reward_value: Int
//)