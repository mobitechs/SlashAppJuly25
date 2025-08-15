package com.mobitechs.slashapp.data.model

data class SpinWheelCampaignDetailsResponse(
    val data: SpinWheelCampaignDetailsItem,
    val success: Boolean
)

data class SpinWheelCampaignDetailsItem(
    val campaign: CampaignData,
    val can_spin: Boolean,
    val remaining_spins: Int,
    val today_spins: Int,
    val wheel_segments: List<WheelSegmentItems>
)

data class CampaignData(
    val description: String,
    val id: Int,
    val max_attempts_per_interval: Int,
    val title: String
)

data class WheelSegmentItems(
    val color: String,
    val id: Int,
    val reward_type: String,
    val reward_value: Int,
    val text: String
)

