package com.mobitechs.slashapp.data.model

data class SpinWheelSummeryResponse(
    val data: SpinWheelSummeryData,
    val success: Boolean
)

data class SpinWheelSummeryData(
    val today_status: List<TodaySpinStatusData>,
    val total_statistics: SpinTotalStatisticsData
)

data class TodaySpinStatusData(
    val campaign_id: Int,
    val can_spin: Boolean,
    val spins_remaining: Int,
    val spins_used: Int,
    val title: String
)

data class SpinTotalStatisticsData(
    val total_cashback_won: Int,
    val total_coupons_won: Int,
    val total_spins: Int,
    val total_wins: Int,
    val win_rate: Int
)