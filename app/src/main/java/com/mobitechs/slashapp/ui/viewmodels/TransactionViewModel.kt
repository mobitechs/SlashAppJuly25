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
    val storeId: Int = 0,
    val storeDetails: StoreListItem? = null,

    // Transaction fields
    val billAmount: String = "",
    val billAmountError: String = "",
    val availableCashback: Double = 500.0,
    val enteredCashback: Double = 0.0,
    val maxAllowedCashback: Double = 0.0,
    val couponCode: String = "",
    val couponError: String = "",
    val isCouponApplied: Boolean = false,
    val appliedCouponDetails: String = "",
    val isCouponLoading: Boolean = false,

    // Calculated fields
    val vendorDiscount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val totalSavings: Double = 0.0,
    val isVendorDiscountApplicable: Boolean = false,

    val navigateToPayment: Boolean = false
) {
    val isPayButtonEnabled: Boolean
        get() = billAmount.isNotEmpty() &&
                billAmount.toDoubleOrNull() != null &&
                billAmount.toDoubleOrNull()!! > 0 &&
                billAmountError.isEmpty() &&
                !isLoading

    val cashbackPercentage: String
        get() = if (billAmount.toDoubleOrNull() != null && billAmount.toDoubleOrNull()!! > 0) {
            val percentage = (enteredCashback / billAmount.toDoubleOrNull()!!) * 100
            String.format("%.1f%%", percentage)
        } else "0%"
}

class TransactionViewModel(
    private val qrScannerRepository: QRScannerRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun loadStoreDetails(storeId: Int) {
        _uiState.update { it.copy(isLoading = true, error = "", storeId = storeId) }

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
            val billAmountValue = amount.toDoubleOrNull() ?: 0.0

            // Calculate max allowed cashback (20% of bill amount or available cashback, whichever is less)
            val maxCashback = if (billAmountValue > 0) {
                minOf(currentState.availableCashback, billAmountValue * 0.2)
            } else {
                0.0
            }

            // Adjust entered cashback if it exceeds the new limit
            val adjustedCashback = if (currentState.enteredCashback > maxCashback) {
                maxCashback
            } else {
                currentState.enteredCashback
            }

            val updatedState = currentState.copy(
                billAmount = amount,
                billAmountError = error,
                maxAllowedCashback = maxCashback,
                enteredCashback = adjustedCashback
            )
            calculateTotals(updatedState)
        }
    }

    fun onCashbackChange(amount: String) {
        val numericAmount = amount.toDoubleOrNull() ?: 0.0

        _uiState.update { currentState ->
            val validAmount = when {
                numericAmount < 0 -> 0.0
                numericAmount > currentState.maxAllowedCashback -> currentState.maxAllowedCashback
                else -> numericAmount
            }

            val updatedState = currentState.copy(enteredCashback = validAmount)
            calculateTotals(updatedState)
        }
    }

    fun onMaxCashbackClick() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(enteredCashback = currentState.maxAllowedCashback)
            calculateTotals(updatedState)
        }
        showToast("Maximum cashback applied!")
    }

    fun onCouponChange(code: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                couponCode = code,
                couponError = "",
                // Don't reset applied status while typing
            )
            updatedState
        }
    }

    fun applyCoupon() {
        val currentState = _uiState.value
        val couponCode = currentState.couponCode.trim()

        if (couponCode.isEmpty()) {
            _uiState.update { it.copy(couponError = "Please enter a coupon code") }
            return
        }

        if (couponCode.length < 3) {
            _uiState.update { it.copy(couponError = "Coupon code must be at least 3 characters") }
            return
        }

        _uiState.update { it.copy(isCouponLoading = true, couponError = "") }

        viewModelScope.launch {
            try {
                val couponRes = qrScannerRepository.validateCoupon(
                    couponCode,
                    currentState.storeId,
                    currentState.billAmount
                )

                if (couponRes.success) {
                    _uiState.update { currentState ->
                        val discountAmount = couponRes.data.discount_details.discount_amount.toDoubleOrNull() ?: 0.0
                        val couponTitle = couponRes.data.coupon.code ?: "Coupon Applied"


                        val updatedState = currentState.copy(
                            isCouponApplied = true,
                            couponError = "",
                            couponDiscount = discountAmount,
                            appliedCouponDetails = "Coupon($couponTitle)",
                            isCouponLoading = false
                        )
                        calculateTotals(updatedState)
                    }
                    showToast("Coupon applied successfully! ₹${String.format("%.2f", couponRes.data.discount_details.discount_amount.toDouble())} discount")
                } else {
                    _uiState.update {
                        it.copy(
                            couponError = couponRes.message ?: "Invalid coupon code",
                            isCouponLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCouponLoading = false,
                        couponError = e.message ?: "Failed to validate coupon"
                    )
                }
            }
        }
    }

    fun removeCoupon() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(
                couponCode = "",
                isCouponApplied = false,
                couponError = "",
                couponDiscount = 0.0,
                appliedCouponDetails = ""
            )
            calculateTotals(updatedState)
        }
        showToast("Coupon removed")
    }

    private fun calculateTotals(state: TransactionUiState): TransactionUiState {
        val billAmount = state.billAmount.toDoubleOrNull() ?: 0.0

        if (billAmount <= 0) {
            return state.copy(
                vendorDiscount = 0.0,
                tax = 0.0,
                grandTotal = 0.0,
                totalSavings = 0.0,
                isVendorDiscountApplicable = false
            )
        }

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

        // Calculate tax (2.72% tax on original bill amount)
        val tax = billAmount * 0.0272

        // Calculate total savings
        val totalSavings = vendorDiscount + state.enteredCashback + state.couponDiscount

        // Calculate grand total: Bill + Tax - All Discounts
        val subtotal = billAmount + tax - vendorDiscount - state.enteredCashback - state.couponDiscount
        val grandTotal = maxOf(0.0, subtotal) // Ensure total doesn't go negative

        return state.copy(
            vendorDiscount = vendorDiscount,
            tax = tax,
            grandTotal = grandTotal,
            totalSavings = totalSavings,
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
            numericAmount > 100000 -> "Amount cannot exceed ₹1,00,000"
            else -> ""
        }
    }

    fun processPayment() {
        val currentState = _uiState.value

        if (currentState.billAmountError.isNotEmpty()) {
            showToast("Please fix the bill amount error")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Simulate payment processing
                kotlinx.coroutines.delay(2000)

                val paymentDetails = if (currentState.totalSavings > 0) {
                    "Payment Successful!\nAmount Paid: ₹${String.format("%.2f", currentState.grandTotal)}\nYou Saved: ₹${String.format("%.2f", currentState.totalSavings)}!"
                } else {
                    "Payment Successful!\nAmount Paid: ₹${String.format("%.2f", currentState.grandTotal)}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        navigateToPayment = true
                    )
                }

                showToast(paymentDetails)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Payment failed"
                    )
                }
                showToast("Payment failed: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }

    fun onNavigateToPayment() {
        _uiState.update { it.copy(navigateToPayment = false) }
    }
}