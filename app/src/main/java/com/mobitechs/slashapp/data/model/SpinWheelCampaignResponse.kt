package com.mobitechs.slashapp.data.model

data class SpinWheelCampaignResponse(
    val data: List<SpinWheelCampaignItem>,
    val success: Boolean
)

data class SpinWheelCampaignItem(
    val campaign_type: String,
    val can_spin: Boolean,
    val description: String,
    val id: Int,
    val last_spin_time: Any,
    val max_attempts_per_interval: Int,
    val remaining_spins: Int,
    val repeat_interval: String,
    val title: String,
    val today_spins: Int
)