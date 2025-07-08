package com.mobitechs.slashapp.data.model

data class RewardListResponse(
    val success: Boolean,
    val data: List<RewardListItem>
)


data class RewardListItem(
    val id: Int,
    val user_id: Int,
    val transaction_id: Int?,
    val store_id: Int?,
    val reward_type: String,
    val amount: Double,
    val description: String?,
    val credit_debit: String, // "CREDIT" or "DEBIT"
    val created_at: String
)