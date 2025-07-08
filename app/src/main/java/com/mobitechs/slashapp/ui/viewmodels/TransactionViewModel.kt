package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.data.repository.QRScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val storeDetails: StoreListItem? = null,

    // Transaction fields
    val billAmount: String = "",
    val billAmountError: String = "",
    val availableCashback: Double = 500.0, // Mock data
    val enteredCashback: Double = 0.0, // Changed from selectedCashback to enteredCashback
    val couponCode: String = "",
    val couponError: String = "",
    val isCouponApplied: Boolean = false,

    // Calculated fields
    val vendorDiscount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val isVendorDiscountApplicable: Boolean = false, // New field to track discount eligibility

    val navigateToPayment: Boolean = false
) {
    val isPayButtonEnabled: Boolean
        get() = billAmount.isNotEmpty() &&
                billAmount.toDoubleOrNull() != null &&
                billAmount.toDoubleOrNull()!! > 0 &&
                !isLoading
}

class TransactionViewModel(
    private val qrScannerRepository: QRScannerRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun loadStoreDetails(storeId: Int) {
        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val storeDetails = qrScannerRepository.getStoreDetails(storeId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        storeDetails = storeDetails.data
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load store details"
                    )
                }
            }
        }
    }

    fun onBillAmountChange(amount: String) {
        _uiState.update { currentState ->
            val error = validateBillAmount(amount, currentState.storeDetails)
            val updatedState = currentState.copy(
                billAmount = amount,
                billAmountError = error
            )
            calculateTotals(updatedState)
        }
    }

    fun onCashbackChange(amount: Double) {
        _uiState.update { currentState ->
            val billAmount = currentState.billAmount.toDoubleOrNull() ?: 0.0
            val maxCashback = minOf(currentState.availableCashback, billAmount * 0.2) // 20% of bill

            val validAmount = when {
                amount < 0 -> 0.0
                amount > maxCashback -> maxCashback
                else -> amount
            }

            val updatedState = currentState.copy(enteredCashback = validAmount)
            calculateTotals(updatedState)
        }
    }

    fun onCouponChange(code: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                couponCode = code,
                couponError = "",
                isCouponApplied = false,
                couponDiscount = 0.0
            )
            calculateTotals(updatedState)
        }
    }

    fun applyCoupon() {
        val currentState = _uiState.value
        val couponCode = currentState.couponCode.trim()

        if (couponCode.isEmpty()) {
            _uiState.update { it.copy(couponError = "Please enter a coupon code") }
            return
        }

        // Mock coupon validation - replace with actual API call
        val isValidCoupon = listOf("SAVE20", "DISCOUNT10", "WELCOME", "XRTMAS790").contains(couponCode.uppercase())

        if (isValidCoupon) {
            val discount = when (couponCode.uppercase()) {
                "SAVE20" -> 20.0
                "DISCOUNT10" -> 10.0
                "WELCOME" -> 15.0
                "XRTMAS790" -> 25.0
                else -> 0.0
            }

            _uiState.update { currentState ->
                val updatedState = currentState.copy(
                    isCouponApplied = true,
                    couponError = "",
                    couponDiscount = discount
                )
                calculateTotals(updatedState)
            }
        } else {
            _uiState.update { it.copy(couponError = "Invalid coupon code") }
        }
    }

    // New method for removing coupon
    fun removeCoupon() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                couponCode = "",
                isCouponApplied = false,
                couponError = "",
                couponDiscount = 0.0
            )
            calculateTotals(updatedState)
        }
    }

    private fun calculateTotals(state: TransactionUiState): TransactionUiState {
        val billAmount = state.billAmount.toDoubleOrNull() ?: 0.0
        val discountPercentage = state.storeDetails?.normal_discount_percentage?.toDoubleOrNull() ?: 0.0
        val minimumOrderAmount = state.storeDetails?.minimum_order_amount?.toDoubleOrNull() ?: 0.0

        // Check if vendor discount is applicable based on minimum order amount
        val isDiscountApplicable = billAmount >= minimumOrderAmount

        // Calculate vendor discount only if bill amount meets minimum requirement
        val vendorDiscount = if (isDiscountApplicable && discountPercentage > 0) {
            (billAmount * discountPercentage) / 100.0
        } else {
            0.0
        }

        // Calculate tax (example: 2.72% tax)
        val tax = billAmount * 0.0272

        // Calculate grand total
        val subtotal = billAmount + tax - vendorDiscount - state.enteredCashback - state.couponDiscount
        val grandTotal = maxOf(0.0, subtotal) // Ensure total doesn't go negative

        return state.copy(
            vendorDiscount = vendorDiscount,
            tax = tax,
            grandTotal = grandTotal,
            isVendorDiscountApplicable = isDiscountApplicable
        )
    }

    private fun validateBillAmount(amount: String, storeDetails: StoreListItem?): String {
        val numericAmount = amount.toDoubleOrNull()
        val minimumOrderAmount = storeDetails?.minimum_order_amount?.toDoubleOrNull() ?: 0.0

        return when {
            amount.isEmpty() -> ""
            numericAmount == null -> "Invalid amount"
            numericAmount <= 0 -> "Amount must be greater than 0"
            numericAmount < minimumOrderAmount && minimumOrderAmount > 0 ->
                "Minimum order amount is ₹${String.format("%.2f", minimumOrderAmount)}"
            else -> ""
        }
    }

    fun processPayment() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Simulate payment processing
                kotlinx.coroutines.delay(2000)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigateToPayment = true
                    )
                }

                showToast("Payment successful!")

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Payment failed"
                    )
                }
            }
        }
    }

    fun onNavigateToPayment() {
        _uiState.update { it.copy(navigateToPayment = false) }
    }
}