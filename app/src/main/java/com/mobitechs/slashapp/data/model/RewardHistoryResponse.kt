package com.mobitechs.slashapp.data.model

data class RewardHistoryResponse(
    val `data`: RewardHistoryData,
    val success: Boolean
)

data class RewardHistoryData(
    val history: List<RewardHistory>,
    val pagination: Pagination,
    val summary: RewardSummary
)

data class RewardHistory(
    val amount: Int,
    val bill_amount: Int,
    val cashback_earned: Int,
    val cashback_used: Int,
    val coupon_discount: Int,
    val coupon_name: String,
    val date: String,
    val final_amount: Int,
    val id: Int,
    val is_transaction: Boolean,
    val reward_for: String,
    val store_name: String,
    val transaction_number: String,
    val type: String,
    val vendor_discount: Int
)

data class RewardSummary(
    val net_rewards: Int,
    val total_credits: Int,
    val total_debits: Int
)