package com.mobitechs.slashapp.data.model

data class SpinWheelResultResponse(
    val data: SpinWheelResultData,
    val success: Boolean
)


data class SpinWheelResultData(
    val next_spin_available: String,
    val remaining_spins: Int,
    val reward: RewardData
)


data class RewardData(
    val display_color: String,
    val display_text: String,
    val id: Int,
    val is_winner: Boolean,
    val message: String,
    val type: String,
    val value: Int
)