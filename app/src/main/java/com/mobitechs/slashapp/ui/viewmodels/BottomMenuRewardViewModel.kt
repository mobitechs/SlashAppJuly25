package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.AvailableCoupon
import com.mobitechs.slashapp.data.model.RewardHistory
import com.mobitechs.slashapp.data.model.RewardSummeryData
import com.mobitechs.slashapp.data.repository.RewardsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Reward UI State
data class RewardUiState(
    // Summary Data
    val rewardSummary: RewardSummeryData? = null,
    val isSummaryLoading: Boolean = false,
    val summaryError: String = "",

    // Available Coupons
    val availableCoupons: List<AvailableCoupon> = emptyList(),
    val isCouponsLoading: Boolean = false,
    val couponsError: String = "",

    // Reward History
    val rewardHistory: List<RewardHistory> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val isLoadingMoreHistory: Boolean = false,
    val historyError: String = "",
    val hasMoreHistoryPages: Boolean = true,
    val currentHistoryPage: Int = 1,

    // UI State
    val selectedTab: Int = 0, // 0 = Rewards, 1 = History
    val isRefreshing: Boolean = false
)

class BottomMenuRewardViewModel(
    private val rewardRepository: RewardsRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    private val pageLimit = 10
    private var loadDataJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadRewardSummary()
        loadAvailableCoupons()
        loadRewardHistory(isInitialLoad = true)
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    private fun loadRewardSummary() {
        _uiState.update { it.copy(isSummaryLoading = true, summaryError = "") }

        viewModelScope.launch {
            try {
                val response = rewardRepository.getRewardStats()

                if (response.success) {
                    _uiState.update {
                        it.copy(
                            rewardSummary = response.data,
                            isSummaryLoading = false,
                            summaryError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSummaryLoading = false,
                            summaryError = "Failed to load reward summary"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSummaryLoading = false,
                        summaryError = e.message ?: "Failed to load reward summary"
                    )
                }
            }
        }
    }

    private fun loadAvailableCoupons() {
        _uiState.update { it.copy(isCouponsLoading = true, couponsError = "") }

        viewModelScope.launch {
            try {
                val response = rewardRepository.getAvailableCoupons()

                if (response.success) {
                    _uiState.update {
                        it.copy(
                            availableCoupons = response.data.available_coupons,
                            isCouponsLoading = false,
                            couponsError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isCouponsLoading = false,
                            couponsError = "Failed to load available coupons"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCouponsLoading = false,
                        couponsError = e.message ?: "Failed to load available coupons"
                    )
                }
            }
        }
    }

    private fun loadRewardHistory(isInitialLoad: Boolean = false) {
        if (!isInitialLoad) {
            _uiState.update { it.copy(isLoadingMoreHistory = true) }
        } else {
            _uiState.update { it.copy(isHistoryLoading = true) }
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val page = if (isInitialLoad) 1 else currentState.currentHistoryPage

                val response = rewardRepository.getRewardHistory(
                    page = page.toString(),
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val newHistory = response.data.history

                    _uiState.update { latestState ->
                        val updatedHistory = if (isInitialLoad) {
                            newHistory
                        } else {
                            // Remove duplicates by filtering out history that already exists
                            val existingIds = latestState.rewardHistory.map { it.id }.toSet()
                            val filteredNewHistory = newHistory.filter { it.id !in existingIds }
                            latestState.rewardHistory + filteredNewHistory
                        }

                        latestState.copy(
                            rewardHistory = updatedHistory,
                            isHistoryLoading = false,
                            isLoadingMoreHistory = false,
                            historyError = "",
                            currentHistoryPage = page + 1,
                            hasMoreHistoryPages = newHistory.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            isLoadingMoreHistory = false,
                            historyError = "Failed to load reward history"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        isLoadingMoreHistory = false,
                        historyError = e.message ?: "Failed to load reward history"
                    )
                }
            }
        }
    }

    fun loadMoreRewardHistory() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMoreHistory && currentState.hasMoreHistoryPages) {
            loadRewardHistory(isInitialLoad = false)
        }
    }

    fun filterHistoryByType(rewardType: String) {
        _uiState.update {
            it.copy(
                isHistoryLoading = true,
                rewardHistory = emptyList(),
                historyError = "",
                currentHistoryPage = 1,
                hasMoreHistoryPages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = rewardRepository.getRewardHistoryWithFilter(
                    rewardType = rewardType,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val history = response.data.history
                    _uiState.update {
                        it.copy(
                            rewardHistory = history,
                            isHistoryLoading = false,
                            historyError = "",
                            currentHistoryPage = 2,
                            hasMoreHistoryPages = history.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            historyError = "Failed to filter reward history"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        historyError = e.message ?: "Failed to filter reward history"
                    )
                }
            }
        }
    }

    fun filterHistoryByDateRange(dateFrom: String, dateTo: String) {
        _uiState.update {
            it.copy(
                isHistoryLoading = true,
                rewardHistory = emptyList(),
                historyError = "",
                currentHistoryPage = 1,
                hasMoreHistoryPages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = rewardRepository.getRewardHistoryWithDateRange(
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val history = response.data.history
                    _uiState.update {
                        it.copy(
                            rewardHistory = history,
                            isHistoryLoading = false,
                            historyError = "",
                            currentHistoryPage = 2,
                            hasMoreHistoryPages = history.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            historyError = "Failed to filter reward history by date"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        historyError = e.message ?: "Failed to filter reward history by date"
                    )
                }
            }
        }
    }

    fun refreshData() {
        // Cancel any ongoing operations
        loadDataJob?.cancel()

        _uiState.update {
            it.copy(
                isRefreshing = true,
                rewardHistory = emptyList(),
                currentHistoryPage = 1,
                hasMoreHistoryPages = true,
                historyError = "",
                couponsError = "",
                summaryError = ""
            )
        }

        viewModelScope.launch {
            loadRewardSummary()
            loadAvailableCoupons()
            loadRewardHistory(isInitialLoad = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retryLoadSummary() {
        loadRewardSummary()
    }

    fun retryLoadCoupons() {
        loadAvailableCoupons()
    }

    fun retryLoadHistory() {
        _uiState.update { it.copy(rewardHistory = emptyList(), isHistoryLoading = true) }
        loadRewardHistory(isInitialLoad = true)
    }

    fun clearSummaryError() {
        _uiState.update { it.copy(summaryError = "") }
    }

    fun clearCouponsError() {
        _uiState.update { it.copy(couponsError = "") }
    }

    fun clearHistoryError() {
        _uiState.update { it.copy(historyError = "") }
    }

    // Helper function to format amounts
    fun formatAmount(amount: Int): String {
        return "₹$amount"
    }

    // Helper function to format date
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank() || dateString == "null") {
            return "22 July 2023"
        }

        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return "22 July 2023")
        } catch (e: Exception) {
            // Try alternative format without milliseconds
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: return "22 July 2023")
            } catch (e: Exception) {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    val outputFormat = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    outputFormat.format(date ?: return "22 July 2023")
                } catch (e: Exception) {
                    "22 July 2023"
                }
            }
        }
    }

    // Helper function to format time
    fun formatTime(dateString: String?): String {
        if (dateString.isNullOrBlank() || dateString == "null") {
            return "11:25 pm"
        }

        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return "11:25 pm")
        } catch (e: Exception) {
            // Try alternative formats
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: return "11:25 pm")
            } catch (e: Exception) {
                "11:25 pm"
            }
        }
    }

    // Helper function to get reward type color
    fun getRewardTypeColor(type: String?): androidx.compose.ui.graphics.Color {
        return when (type?.lowercase()) {
            "credit" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            "debit" -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Pink/Red
            "cashback" -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
            "coupon" -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
            else -> androidx.compose.ui.graphics.Color(0xFF607D8B) // Blue Grey
        }
    }
}