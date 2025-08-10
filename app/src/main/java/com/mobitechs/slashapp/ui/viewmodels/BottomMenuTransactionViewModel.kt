package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.MyTransactionListItem
import com.mobitechs.slashapp.data.model.TransactionStatsData
import com.mobitechs.slashapp.data.repository.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Transaction List UI State
data class TransactionListUiState(
    // Transactions
    val transactions: List<MyTransactionListItem> = emptyList(),
    val isTransactionsLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val transactionsError: String = "",
    val hasMorePages: Boolean = true,
    val currentPage: Int = 1,

    // Search
    val searchQuery: String = "",
    val isSearching: Boolean = false,

    // Stats
    val transactionStats: TransactionStatsData? = null,
    val isStatsLoading: Boolean = false,
    val statsError: String = "",

    // General states
    val isRefreshing: Boolean = false
)

class BottomMenuTransactionViewModel(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val pageLimit = 10

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadTransactionStats()
        loadTransactions(isInitialLoad = true)
    }

    private fun loadTransactionStats() {
        _uiState.update { it.copy(isStatsLoading = true, statsError = "") }

        viewModelScope.launch {
            try {
                val response = transactionRepository.getTransactionStats()

                if (response.success) {
                    _uiState.update {
                        it.copy(
                            transactionStats = response.data,
                            isStatsLoading = false,
                            statsError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isStatsLoading = false,
                            statsError = "Failed to load transaction stats"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStatsLoading = false,
                        statsError = e.message ?: "Failed to load transaction stats"
                    )
                }
            }
        }
    }

    private fun loadTransactions(isInitialLoad: Boolean = false) {
        if (!isInitialLoad) {
            _uiState.update { it.copy(isLoadingMore = true) }
        } else {
            _uiState.update { it.copy(isTransactionsLoading = true) }
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val page = if (isInitialLoad) 1 else currentState.currentPage

                val response = transactionRepository.getMyTransactionList(
                    page = page.toString(),
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val newTransactions = response.data

                    _uiState.update { latestState ->
                        val updatedTransactions = if (isInitialLoad) {
                            newTransactions
                        } else {
                            // Remove duplicates by filtering out transactions that already exist
                            val existingIds = latestState.transactions.map { it.id }.toSet()
                            val filteredNewTransactions = newTransactions.filter { it.id !in existingIds }
                            latestState.transactions + filteredNewTransactions
                        }

                        latestState.copy(
                            transactions = updatedTransactions,
                            isTransactionsLoading = false,
                            isLoadingMore = false,
                            transactionsError = "",
                            currentPage = page + 1,
                            hasMorePages = newTransactions.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            isLoadingMore = false,
                            transactionsError = "Failed to load transactions"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTransactionsLoading = false,
                        isLoadingMore = false,
                        transactionsError = e.message ?: "Failed to load transactions"
                    )
                }
            }
        }
    }

    fun loadMoreTransactions() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMore && currentState.hasMorePages && currentState.searchQuery.isEmpty()) {
            loadTransactions(isInitialLoad = false)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            // If search is cleared, reload all transactions
            _uiState.update {
                it.copy(
                    transactions = emptyList(),
                    currentPage = 1,
                    hasMorePages = true,
                    transactionsError = "",
                    isTransactionsLoading = true
                )
            }
            loadTransactions(isInitialLoad = true)
        } else {
            // Debounce search
            searchJob = viewModelScope.launch {
                delay(500) // 500ms debounce
                searchTransactions(query)
            }
        }
    }

    private fun searchTransactions(query: String) {
        _uiState.update {
            it.copy(
                isSearching = true,
                isTransactionsLoading = true,
                transactions = emptyList(),
                transactionsError = "",
                currentPage = 1,
                hasMorePages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = transactionRepository.getMyTransactionListWithSearchFilter(
                    search = query,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val transactions = response.data
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            isTransactionsLoading = false,
                            isSearching = false,
                            transactionsError = "",
                            currentPage = 2,
                            hasMorePages = transactions.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            isSearching = false,
                            transactionsError = "Failed to search transactions"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTransactionsLoading = false,
                        isSearching = false,
                        transactionsError = e.message ?: "Failed to search transactions"
                    )
                }
            }
        }
    }

    fun loadMoreSearchResults() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMore && currentState.hasMorePages && currentState.searchQuery.isNotEmpty()) {
            _uiState.update { it.copy(isLoadingMore = true) }

            viewModelScope.launch {
                try {
                    val response = transactionRepository.getMyTransactionListWithSearchFilter(
                        search = currentState.searchQuery,
                        page = currentState.currentPage.toString(),
                        limit = pageLimit.toString()
                    )

                    if (response.success) {
                        val newTransactions = response.data
                        _uiState.update { latestState ->
                            // Remove duplicates
                            val existingIds = latestState.transactions.map { it.id }.toSet()
                            val filteredNewTransactions = newTransactions.filter { it.id !in existingIds }

                            latestState.copy(
                                transactions = latestState.transactions + filteredNewTransactions,
                                isLoadingMore = false,
                                currentPage = latestState.currentPage + 1,
                                hasMorePages = newTransactions.size >= pageLimit
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                transactionsError = "Failed to load more results"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            transactionsError = e.message ?: "Failed to load more results"
                        )
                    }
                }
            }
        }
    }

    // Date range filter function
    fun filterByDateRange(dateFrom: String, dateTo: String) {
        _uiState.update {
            it.copy(
                isTransactionsLoading = true,
                transactions = emptyList(),
                transactionsError = "",
                currentPage = 1,
                hasMorePages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = transactionRepository.getMyTransactionListWithDateRangeFilter(
                    date_from = dateFrom,
                    date_to = dateTo,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val transactions = response.data
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            isTransactionsLoading = false,
                            transactionsError = "",
                            currentPage = 2,
                            hasMorePages = transactions.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            transactionsError = "Failed to filter transactions by date"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTransactionsLoading = false,
                        transactionsError = e.message ?: "Failed to filter transactions by date"
                    )
                }
            }
        }
    }

    // Amount range filter function
    fun filterByAmountRange(minAmount: String, maxAmount: String) {
        _uiState.update {
            it.copy(
                isTransactionsLoading = true,
                transactions = emptyList(),
                transactionsError = "",
                currentPage = 1,
                hasMorePages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = transactionRepository.getMyTransactionListWithAmountFilter(
                    min_amount = minAmount,
                    max_amount = maxAmount,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val transactions = response.data
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            isTransactionsLoading = false,
                            transactionsError = "",
                            currentPage = 2,
                            hasMorePages = transactions.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            transactionsError = "Failed to filter transactions by amount"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTransactionsLoading = false,
                        transactionsError = e.message ?: "Failed to filter transactions by amount"
                    )
                }
            }
        }
    }

    // Store and status filter function
    fun filterByStoreAndStatus(storeId: String, status: String) {
        _uiState.update {
            it.copy(
                isTransactionsLoading = true,
                transactions = emptyList(),
                transactionsError = "",
                currentPage = 1,
                hasMorePages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = transactionRepository.getMyTransactionListWithStoreAndStatusFilter(
                    store_id = storeId,
                    status = status,
                    page = "1",
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val transactions = response.data
                    _uiState.update {
                        it.copy(
                            transactions = transactions,
                            isTransactionsLoading = false,
                            transactionsError = "",
                            currentPage = 2,
                            hasMorePages = transactions.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTransactionsLoading = false,
                            transactionsError = "Failed to filter transactions by store and status"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTransactionsLoading = false,
                        transactionsError = e.message ?: "Failed to filter transactions by store and status"
                    )
                }
            }
        }
    }

    fun refreshData() {
        // Cancel any ongoing operations
        searchJob?.cancel()

        _uiState.update {
            it.copy(
                isRefreshing = true,
                searchQuery = "",
                transactions = emptyList(),
                currentPage = 1,
                hasMorePages = true,
                transactionsError = ""
            )
        }

        viewModelScope.launch {
            loadTransactionStats()
            loadTransactions(isInitialLoad = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retryLoadTransactions() {
        val currentState = _uiState.value
        when {
            currentState.searchQuery.isNotEmpty() -> {
                searchTransactions(currentState.searchQuery)
            }
            else -> {
                _uiState.update { it.copy(transactions = emptyList(), isTransactionsLoading = true) }
                loadTransactions(isInitialLoad = true)
            }
        }
    }

    fun clearTransactionsError() {
        _uiState.update { it.copy(transactionsError = "") }
    }

    fun clearStatsError() {
        _uiState.update { it.copy(statsError = "") }
    }

    // Helper function to format amounts
    fun formatAmount(amount: String): String {
        return try {
            val doubleValue = amount.toDouble()
            if (doubleValue == doubleValue.toInt().toDouble()) {
                doubleValue.toInt().toString()
            } else {
                String.format("%.2f", doubleValue)
            }
        } catch (e: NumberFormatException) {
            amount
        }
    }

    // Helper function to format date
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }

    // Helper function to format time
    fun formatTime(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }
}