package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.MyTransactionListItem
import com.mobitechs.slashapp.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Transaction Details UI State
data class TransactionDetailsUiState(
    val transaction: MyTransactionListItem? = null,
    val isLoading: Boolean = false,
    val error: String = ""
)

class TransactionDetailsViewModel(
    private val transactionRepository: TransactionRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    fun loadTransactionDetails(transactionId: String) { // Changed to String
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                // Convert string to int for comparison
                val targetTransactionId = transactionId.toIntOrNull()
                if (targetTransactionId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid transaction ID"
                        )
                    }
                    return@launch
                }

                val response = transactionRepository.getMyTransactionList("1", "100")

                if (response.success) {
                    val transaction = response.data.find { it.id == targetTransactionId }
                    if (transaction != null) {
                        _uiState.update {
                            it.copy(
                                transaction = transaction,
                                isLoading = false,
                                error = ""
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Transaction not found"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load transaction details"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load transaction details"
                    )
                }
            }
        }
    }

    fun retryLoadTransaction(transactionId: String) { // Changed to String
        loadTransactionDetails(transactionId)
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

    // Helper function to format date and time
    fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("MM-dd-yy h:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return dateString)
        } catch (e: Exception) {
            dateString
        }
    }

    // Helper function to calculate total savings
    fun calculateTotalSavings(transaction: MyTransactionListItem): String {
        return try {
            val billAmount = transaction.bill_amount.toDouble()
            val finalAmount = transaction.final_amount.toDouble()
            val savings = billAmount - finalAmount
            formatAmount(savings.toString())
        } catch (e: Exception) {
            "0"
        }
    }
}