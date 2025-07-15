package com.mobitechs.slashapp.data.model


data class UPIPaymentRequest(
    val payeeName: String,
    val payeeVPA: String, // UPI ID
    val amount: String,
    val note: String = "",
    val transactionId: String = "",
    val transactionRef: String = "",
    val selectedAppPackage: String? = null
)

data class UPIPaymentResult(
    val isSuccess: Boolean,
    val transactionId: String = "",
    val responseCode: String = "",
    val status: String = "",
    val txnRef: String = "",
    val errorMessage: String = ""
)

data class SaveTransactionRequest(
    val store_id: Int,
    val bill_amount: String,
    val cashback_used: String,
    val coupon_id: String? = null,
    val coupon_discount: String,
    val vendor_discount: String,
    val tax_amount: String,
    val final_amount: String,
    val transaction_number: String,
    val upi_response_code: String,
    val payment_method: String,
    val payment_status: String,
    val comment: String,
    val error_msg: String


)

// Transaction save response
data class UpdateTransactionResponse(
    val success: Boolean,
    val message: String,
    val cashback_earned: String

)

data class TransactionData(
    val transaction_id: String,
    val cashback_earned: String,
    val updated_wallet_balance: String
)