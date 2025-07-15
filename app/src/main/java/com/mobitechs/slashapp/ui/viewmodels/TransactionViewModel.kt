// TransactionViewModel.kt
package com.mobitechs.slashapp.ui.viewmodels

import android.app.Activity
import androidx.compose.ui.unit.Constraints
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.*
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.data.repository.QRScannerRepository
import com.mobitechs.slashapp.utils.Constants
import com.mobitechs.slashapp.utils.PersonalUPIPaymentManager
import com.mobitechs.slashapp.utils.UPIApp
import com.mobitechs.slashapp.utils.generateTransactionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TransactionUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val storeId: Int = 0,
    val transactionId: String = "",
    val storeDetails: StoreListItem? = null,

    // Transaction fields
    val billAmount: String = "",
    val billAmountError: String = "",
    val availableCashback: Double = 0.0,
    val enteredCashback: Double = 0.0,
    val maxAllowedCashback: Double = 0.0,
    val couponId: String = "",
    val couponCode: String = "",
    val couponError: String = "",
    val isCouponApplied: Boolean = false,
    val appliedCouponDetails: String = "",
    val isCouponLoading: Boolean = false,

    // User wallet details from API
    val totalCashbackEarned: Double = 0.0,
    val totalCashbackRedeemed: Double = 0.0,
    val totalCouponRedeemed: Double = 0.0,

    // Calculated fields
    val vendorDiscount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val tax: Double = 0.0,
    var grandTotal: Double = 0.0,
    val totalSavings: Double = 0.0,
    val isVendorDiscountApplicable: Boolean = false,

    // UPI Apps Selection
    val showUPIAppsDialog: Boolean = false,
    val availableUPIApps: List<UPIApp> = emptyList(),

    // UPI Payment fields
    val launchUPIPayment: UPIPaymentRequest? = null,
    val isPaymentProcessing: Boolean = false,
    val paymentError: String = "",

    val cashback_earned: String = "",

    val navigateToPayment: Boolean = false,
    val isProfileLoading: Boolean = false
) {
    val isPayButtonEnabled: Boolean
        get() = billAmount.isNotEmpty() &&
                billAmount.toDoubleOrNull() != null &&
                billAmount.toDoubleOrNull()!! > 0 &&
                billAmountError.isEmpty() &&
                !isLoading &&
                !isPaymentProcessing

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

    init {
        // Load user profile to get latest available cashback when ViewModel is created
        loadUserProfile()


    }

    private fun loadUserProfile() {
        _uiState.update {
            it.copy(isProfileLoading = true)
        }

        viewModelScope.launch {
            try {
                val profileResponse = authRepository.getUserDetails()
                if (profileResponse.success) {
                    val user = profileResponse.data
                    val availableCashback = user.available_cashback.toDoubleOrNull() ?: 0.0
                    val totalEarned = user.total_cashback_earned.toDoubleOrNull() ?: 0.0
                    val totalRedeemed = user.total_cashback_redeemed.toDoubleOrNull() ?: 0.0
                    val totalCouponRedeemed = user.total_coupon_redeemed.toDoubleOrNull() ?: 0.0

                    _uiState.update { currentState ->
                        val updatedState = currentState.copy(
                            availableCashback = availableCashback,
                            totalCashbackEarned = totalEarned,
                            totalCashbackRedeemed = totalRedeemed,
                            totalCouponRedeemed = totalCouponRedeemed,
                            isProfileLoading = false
                        )

                        // Recalculate max allowed cashback based on new available cashback
                        val billAmountValue = currentState.billAmount.toDoubleOrNull() ?: 0.0
                        val maxCashback = if (billAmountValue > 0) {
                            minOf(availableCashback, billAmountValue * 0.2)
                        } else {
                            0.0
                        }

                        // Adjust entered cashback if it exceeds the new limit
                        val adjustedCashback = if (currentState.enteredCashback > maxCashback) {
                            maxCashback
                        } else {
                            currentState.enteredCashback
                        }

                        val finalState = updatedState.copy(
                            maxAllowedCashback = maxCashback,
                            enteredCashback = adjustedCashback
                        )
                        calculateTotals(finalState)
                    }
                } else {
                    _uiState.update { it.copy(isProfileLoading = false) }
                    showToast("Failed to load wallet details: ${profileResponse.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProfileLoading = false) }
                showToast("Failed to load wallet details: ${e.message}")
            }
        }
    }

    fun loadStoreDetails(storeId: Int) {
        _uiState.update { it.copy(isLoading = true, error = "", storeId = storeId) }

        viewModelScope.launch {
            try {
                val storeDetails = qrScannerRepository.getStoreDetails(storeId)
                _uiState.update { currentState ->
                    val updatedState = currentState.copy(
                        isLoading = false,
                        storeDetails = storeDetails.data
                    )
                    calculateTotals(updatedState)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load store details"
                    )
                }
                showToast("Failed to load store details: ${e.message}")
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
                numericAmount > currentState.availableCashback -> currentState.availableCashback
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

    fun onClearCashback() {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(enteredCashback = 0.0)
            calculateTotals(updatedState)
        }
    }

    fun onCouponChange(code: String) {
        _uiState.update { currentState ->
            currentState.copy(
                couponCode = code.uppercase(), // Auto uppercase coupon codes
                couponError = ""
            )
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
                            couponId = couponRes.data.coupon.id.toString(),
                            couponError = "",
                            couponDiscount = discountAmount,
                            appliedCouponDetails = couponTitle,
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

    // UPI Payment functions
    fun initiateUPIPayment() {
        _uiState.update {
            it.copy(transactionId = generateTransactionId())
        }
        val currentState = _uiState.value

        if (currentState.billAmountError.isNotEmpty()) {
            showToast("Please fix the bill amount error")
            return
        }

        if (currentState.enteredCashback > currentState.availableCashback) {
            showToast("Insufficient cashback balance")
            return
        }

        if (currentState.grandTotal <= 0) {
            showToast("Invalid payment amount")
            return
        }

        // Show UPI apps selection instead of directly launching payment
        _uiState.update {
            it.copy(showUPIAppsDialog = true)
        }
        //initiating payment
        initiateTransactionToServer()
    }

    fun loadUPIApps(activity: Activity) {
        viewModelScope.launch {
            try {
//                val upiPaymentManager = UPIPaymentManager(activity)
                val upiPaymentManager = PersonalUPIPaymentManager(activity)
                val upiApps = upiPaymentManager.getUPIApps()

                android.util.Log.d("TransactionViewModel", "Found ${upiApps.size} UPI apps")
                upiApps.forEach { app ->
                    android.util.Log.d("TransactionViewModel", "UPI app: ${app.appName} (${app.packageName})")
                }

                if (upiApps.isEmpty()) {
                    _uiState.update { it.copy(showUPIAppsDialog = false) }
                    showToast("No UPI apps found. Please install a UPI app like Google Pay, PhonePe, or Paytm")
                } else {
                    _uiState.update {
                        it.copy(
                            availableUPIApps = upiApps,
                            showUPIAppsDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TransactionViewModel", "Error loading UPI apps", e)
                _uiState.update { it.copy(showUPIAppsDialog = false) }
                showToast("Error loading UPI apps: ${e.message}")
            }
        }
    }

    fun launchUPIPaymentWithApp(selectedApp: UPIApp? = null) {

        val currentState = _uiState.value
//        val transactionId = "TXN_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        val note = "Payment to ${currentState.storeDetails?.name ?: "Store"}"

        currentState.grandTotal = 10.00
        val upiPaymentRequest = UPIPaymentRequest(
            payeeName = Constants.UPI_PAYEE_NAME,
            payeeVPA = Constants.UPI_PAYEE_VPA,
            amount = String.format("%.2f", currentState.grandTotal),
            note = note,
            transactionId = currentState.transactionId,
            selectedAppPackage = selectedApp?.packageName
        )

        android.util.Log.d("TransactionViewModel", "Launching UPI payment with: ${upiPaymentRequest}")

        _uiState.update {
            it.copy(
                launchUPIPayment = upiPaymentRequest,
                isPaymentProcessing = true,
                paymentError = "",
                showUPIAppsDialog = false
            )
        }
    }

    fun dismissUPIAppsDialog() {
        _uiState.update { it.copy(showUPIAppsDialog = false) }
    }

    fun handleUPIPaymentResult(result: UPIPaymentResult) {
        android.util.Log.d("TransactionViewModel", "UPI payment result: ${result}")

        _uiState.update { it.copy(isPaymentProcessing = false) }

        if (result.isSuccess) {
            // Payment successful - save transaction to server
            updateTransactionToServer(result, Constants.SUCCESS)
        } else {
            clearError()
            val errorMessage = result.errorMessage.ifEmpty { "Payment failed" }
            _uiState.update { it.copy(paymentError = errorMessage) }
            updateTransactionToServer(result, Constants.FAILED)
            showToast("Payment failed: $errorMessage")
        }
    }

    private fun initiateTransactionToServer() {
        val currentState = _uiState.value

        viewModelScope.launch {
            try {
                val saveRequest = SaveTransactionRequest(
                    store_id = currentState.storeId,
                    bill_amount = currentState.billAmount,
                    vendor_discount = String.format("%.2f", currentState.vendorDiscount),
                    cashback_used = String.format("%.2f", currentState.enteredCashback),
                    coupon_id = _uiState.value.couponId,
                    coupon_discount = String.format("%.2f", currentState.couponDiscount),
                    final_amount = String.format("%.2f", currentState.grandTotal),
                    tax_amount = String.format("%.2f", currentState.tax),
                    transaction_number = _uiState.value.transactionId,
                    upi_response_code = "",
                    payment_method = "",
                    payment_status = "Initiate",
                    comment = "",
                    error_msg = _uiState.value.paymentError
                )

                val response = qrScannerRepository.initiateTransaction(saveRequest)

                if (response.success) {

                    showToast("Transaction Initiated Successfully ")

                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            paymentError = response.message ?: "Failed to initiate transaction"
                        )
                    }
                    showToast("Failed to initiate transaction: ${response.message}")
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paymentError = e.message ?: "Failed to save transaction"
                    )
                }
                showToast("Payment completed but failed to save transaction: ${e.message}")
            }
        }
    }

    private fun updateTransactionToServer(upiResult: UPIPaymentResult,paymentStatus:String) {
        val currentState = _uiState.value

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val saveRequest = SaveTransactionRequest(
                    store_id = currentState.storeId,
                    bill_amount = currentState.billAmount,
                    vendor_discount = String.format("%.2f", currentState.vendorDiscount),
                    cashback_used = String.format("%.2f", currentState.enteredCashback),
                    coupon_id = _uiState.value.couponId,
                    coupon_discount = String.format("%.2f", currentState.couponDiscount),
                    final_amount = String.format("%.2f", currentState.grandTotal),
                    tax_amount = String.format("%.2f", currentState.tax),
                    transaction_number = _uiState.value.transactionId,
                    upi_response_code = upiResult.responseCode,
                    payment_method = "UPI",
                    payment_status = paymentStatus,
                    comment = "",
                    error_msg = upiResult.errorMessage
                )

                val response = qrScannerRepository.updateTransaction(_uiState.value.transactionId,saveRequest)

                if (response.success && paymentStatus== Constants.SUCCESS) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToPayment = true ,
                            cashback_earned = response.cashback_earned
                        )
                    }

                    val paymentDetails = if (currentState.totalSavings > 0) {
                        "Payment Successful!\nAmount Paid: ₹${String.format("%.2f", currentState.grandTotal)}\nYou Saved: ₹${String.format("%.2f", currentState.totalSavings)}!"
                    } else {
                        "Payment Successful!\nAmount Paid: ₹${String.format("%.2f", currentState.grandTotal)}"
                    }

                    showToast(paymentDetails)

                    // Refresh user profile after successful payment to update available cashback
                    loadUserProfile()

                } else {
                    var errMsg = response.message
                    if(paymentStatus== Constants.FAILED){
                        errMsg = upiResult.errorMessage
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            paymentError = errMsg
                        )
                    }
                    showToast("Failed Update: ${response.message}")
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paymentError = e.message ?: "Failed to save transaction"
                    )
                }
                showToast("Catchee:  ${e.message}")
            }
        }
    }

    fun onUPIPaymentLaunched() {
        _uiState.update { it.copy(launchUPIPayment = null) }
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
        val isDiscountApplicable = billAmount >= minimumOrderAmount && minimumOrderAmount > 0

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

    fun refreshWalletDetails() {
        loadUserProfile()
    }

    fun clearError() {
        _uiState.update { it.copy(error = "", paymentError = "") }
    }

    fun onNavigateToPayment() {
        _uiState.update { it.copy(navigateToPayment = false) }
    }
}