package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.*
import com.mobitechs.slashapp.data.repository.SpinWheelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DailyRewardsUiState(
    val isLoading: Boolean = false,
    val error: String = "",

    // Summary data
    val summaryData: SpinWheelSummeryData? = null,

    // Campaigns list
    val campaigns: List<SpinWheelCampaignItem> = emptyList(),
    val isCampaignsLoading: Boolean = false,

    // Current campaign details for wheel
    val currentCampaign: CampaignData? = null,
    val wheelSegments: List<WheelSegmentItems> = emptyList(),
    val canSpin: Boolean = false,
    val remainingSpins: Int = 0,
    val todaySpins: Int = 0,

    // Spin animation and result
    val isSpinning: Boolean = false,
    val spinResult: RewardData? = null,
    val showResultDialog: Boolean = false,

    // History
    val spinHistory: List<SpinWheelHistoryItem> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyError: String = "",

    // Navigation state
    val selectedCampaignId: String = "",
    val showSpinWheel: Boolean = false
)

class DailyRewardsViewModel(
    private val spinWheelRepository: SpinWheelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyRewardsUiState())
    val uiState: StateFlow<DailyRewardsUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        loadSummary()
        loadCampaigns()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = "") }

                val summaryResponse = spinWheelRepository.getDailySpinWheelSummery()

                if (summaryResponse.success) {
                    _uiState.update {
                        it.copy(
                            summaryData = summaryResponse.data,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Failed to load summary",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load summary",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadCampaigns() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCampaignsLoading = true) }

                val campaignsResponse = spinWheelRepository.getDailySpinWheelCampaign()

                if (campaignsResponse.success) {
                    _uiState.update {
                        it.copy(
                            campaigns = campaignsResponse.data,
                            isCampaignsLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Failed to load campaigns",
                            isCampaignsLoading = false
                        )
                    }
                    showToast("Failed to load campaigns")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load campaigns",
                        isCampaignsLoading = false
                    )
                }
                showToast("Failed to load campaigns: ${e.message}")
            }
        }
    }

    fun selectCampaign(campaignId: String) {
        _uiState.update {
            it.copy(
                selectedCampaignId = campaignId,
                showSpinWheel = true
            )
        }
        loadCampaignDetails(campaignId)
    }

    private fun loadCampaignDetails(campaignId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = "") }

                val detailsResponse = spinWheelRepository.getDailySpinWheelCampaignDetails(campaignId)

                if (detailsResponse.success) {
                    _uiState.update {
                        it.copy(
                            currentCampaign = detailsResponse.data.campaign,
                            wheelSegments = detailsResponse.data.wheel_segments,
                            canSpin = detailsResponse.data.can_spin,
                            remainingSpins = detailsResponse.data.remaining_spins,
                            todaySpins = detailsResponse.data.today_spins,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Failed to load campaign details",
                            isLoading = false
                        )
                    }
                    showToast("Failed to load campaign details")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load campaign details",
                        isLoading = false
                    )
                }
                showToast("Failed to load campaign details: ${e.message}")
            }
        }
    }

    fun spinWheel() {
        val currentState = _uiState.value

        if (!currentState.canSpin) {
            showToast("No spins remaining today!")
            return
        }

        if (currentState.isSpinning) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSpinning = true, error = "") }

                val spinResponse = spinWheelRepository.spinWheelResult(currentState.selectedCampaignId)

                if (spinResponse.success) {
                    // Simulate spin animation delay
                    kotlinx.coroutines.delay(3000) // 3 seconds spin animation

                    _uiState.update {
                        it.copy(
                            isSpinning = false,
                            spinResult = spinResponse.data.reward,
                            showResultDialog = true,
                            remainingSpins = spinResponse.data.remaining_spins,
                            canSpin = spinResponse.data.remaining_spins > 0
                        )
                    }

                    // Refresh summary after spin
                    loadSummary()
                } else {
                    _uiState.update {
                        it.copy(
                            isSpinning = false,
                            error = "Failed to spin wheel"
                        )
                    }
                    showToast("Failed to spin wheel")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSpinning = false,
                        error = e.message ?: "Failed to spin wheel"
                    )
                }
                showToast("Failed to spin wheel: ${e.message}")
            }
        }
    }

    fun dismissResultDialog() {
        _uiState.update {
            it.copy(
                showResultDialog = false,
                spinResult = null
            )
        }
    }

    fun backFromSpinWheel() {
        _uiState.update {
            it.copy(
                showSpinWheel = false,
                selectedCampaignId = "",
                currentCampaign = null,
                wheelSegments = emptyList(),
                spinResult = null,
                showResultDialog = false
            )
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isHistoryLoading = true, historyError = "") }

                val historyResponse = spinWheelRepository.getSpinWheelHistory()

                if (historyResponse.success) {
                    _uiState.update {
                        it.copy(
                            spinHistory = historyResponse.data.history,
                            isHistoryLoading = false,
                            historyError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            historyError = "Failed to load history",
                            isHistoryLoading = false
                        )
                    }
                    showToast("Failed to load history")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        historyError = e.message ?: "Failed to load history",
                        isHistoryLoading = false
                    )
                }
                showToast("Failed to load history: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }

    fun refreshData() {
        loadDashboardData()
    }
}