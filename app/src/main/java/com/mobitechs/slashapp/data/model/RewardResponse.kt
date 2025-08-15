package com.mobitechs.slashapp.data.model

data class RewardSummeryResponse(
    val data: RewardSummeryData,
    val success: Boolean
)

data class RewardSummeryData(
    val coupons: Coupons,
    val recent_activity: RecentActivity,
    val wallet: Wallet
)

data class Coupons(
    val available_count: Int,
    val used_count: Int
)

data class RecentActivity(
    val credits_last_30_days: Int,
    val debits_last_30_days: Int
)


