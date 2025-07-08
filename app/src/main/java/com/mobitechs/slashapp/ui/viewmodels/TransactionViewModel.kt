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
    val billAmount: String = "",
    val billAmountError: String = "",
    val availableCashback: Double = 0.0,
    val selectedCashback: Double = 0.0,
    val couponCode: String = "",
    val couponError: String = "",
    val isCouponApplied: Boolean = false,
    val couponDiscount: Double = 0.0,
    val vendorDiscount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val isPayButtonEnabled: Boolean = false,
    val cashbackEarned: Double = 0.0,
    val navigateToPayment: Boolean = false,
    val paymentUrl: String = ""
)


class TransactionViewModel(
    private val qrScannerRepository: QRScannerRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadUserWallet()
    }

    private fun loadUserWallet() {
        viewModelScope.launch {
            try {
                val wallet = qrScannerRepository.getUserWallet()
                _uiState.update {
                    it.copy(availableCashback = wallet.available_cashback.toDoubleOrNull() ?: 0.0)
                }
            } catch (e: Exception) {
                // Handle error silently or show in UI
            }
        }
    }

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
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }

        _uiState.update {
            it.copy(
                billAmount = filteredAmount,
                billAmountError = validateBillAmount(filteredAmount),
                // Reset coupon if amount changes
                isCouponApplied = false,
                couponDiscount = 0.0,
                couponError = ""
            )
        }

        calculateTotals()
    }

    private fun validateBillAmount(amount: String): String {
        val currentState = uiState.value
        return when {
            amount.isEmpty() -> "Bill amount is required"
            amount.toDoubleOrNull() == null -> "Invalid amount"
            amount.toDouble() <= 0 -> "Amount must be greater than 0"
            amount.toDouble() < (currentState.storeDetails?.minimum_order_amount?.toDouble() ?: 0.0) ->
                "Minimum order amount is ₹${currentState.storeDetails?.minimum_order_amount}"
            else -> ""
        }
    }

    fun onCashbackChange(cashback: Double) {
        val billAmount = uiState.value.billAmount.toDoubleOrNull() ?: 0.0
        val maxCashback = minOf(uiState.value.availableCashback, billAmount * 0.2) // 20% limit

        _uiState.update {
            it.copy(
                selectedCashback = minOf(cashback, maxCashback),
                // Reset coupon when cashback is used
                isCouponApplied = false,
                couponDiscount = 0.0,
                couponError = ""
            )
        }

        calculateTotals()
    }

    fun onCouponChange(coupon: String) {
        _uiState.update {
            it.copy(
                couponCode = coupon.uppercase(),
                couponError = "",
                isCouponApplied = false,
                couponDiscount = 0.0
            )
        }

        calculateTotals()
    }

    fun applyCoupon() {
        val currentState = uiState.value
        val couponCode = currentState.couponCode
        val storeId = currentState.storeDetails?.id ?: return
        val billAmount = currentState.billAmount.toDoubleOrNull() ?: return

        if (couponCode.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val couponData = qrScannerRepository.validateCoupon(couponCode, storeId, billAmount)

                if (couponData.data.is_active) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCouponApplied = true,
                            couponDiscount = couponData.data.discount_amount.toDouble(),
                            selectedCashback = 0.0, // Reset cashback when coupon is applied
                            couponError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            couponError = "Invalid or expired coupon"
                        )
                    }
                }

                calculateTotals()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        couponError = e.message ?: "Failed to apply coupon"
                    )
                }
            }
        }
    }

    private fun calculateTotals() {
        val currentState = uiState.value
        val billAmount = currentState.billAmount.toDoubleOrNull() ?: 0.0
        val store = currentState.storeDetails

        if (billAmount <= 0 || store == null) {
            _uiState.update {
                it.copy(
                    vendorDiscount = 0.0,
                    tax = 0.0,
                    grandTotal = 0.0,
                    isPayButtonEnabled = false
                )
            }
            return
        }

        // Calculate vendor discount based on user type (VIP or normal)
        val user = authRepository.getCurrentUser()
        val discountPercentage = if (user?.profile_completion_percentage == 100) { // Assuming VIP users have 100% profile
            store.vip_discount_percentage
        } else {
            store.normal_discount_percentage
        }

        val vendorDiscount = billAmount * (discountPercentage.toInt() / 100)
        val tax = billAmount * 0.02 // 2% tax

        val finalAmount = billAmount + tax - vendorDiscount - currentState.selectedCashback - currentState.couponDiscount

        // Calculate cashback earned (1% of final amount)
        val cashbackEarned = finalAmount * 0.01

        _uiState.update {
            it.copy(
                vendorDiscount = vendorDiscount,
                tax = tax,
                grandTotal = maxOf(finalAmount, 0.0),
                cashbackEarned = cashbackEarned,
                isPayButtonEnabled = currentState.billAmountError.isEmpty() && billAmount > 0
            )
        }
    }

    fun processPayment() {
        val currentState = uiState.value
        val store = currentState.storeDetails ?: return
        val billAmount = currentState.billAmount.toDoubleOrNull() ?: return

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val transactionData = qrScannerRepository.createTransaction(
                    storeId = store.id,
                    billAmount = billAmount,
                    vendorDiscount = currentState.vendorDiscount,
                    cashbackUsed = currentState.selectedCashback,
                    couponCode = if (currentState.isCouponApplied) currentState.couponCode else null,
                    couponDiscount = currentState.couponDiscount,
                    finalAmount = currentState.grandTotal,
                    paymentMethod = "UPI", // Default payment method
                    comment = null
                )
                if(transactionData.success){
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToPayment = true,
                            // paymentUrl = transactionData.payment_url ?: ""
                        )
                    }
                }else{
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Not able to process the transaction"
                        )
                    }
                }



            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to process payment"
                    )
                }
            }
        }
    }

    fun onNavigateToPayment() {
        _uiState.update { it.copy(navigateToPayment = false) }
    }
}