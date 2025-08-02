package com.mobitechs.slashapp.data.model


data class TransactionsInitiateResponse(
    val data: TransactionsInitiateResponseItem,
    val message: String,
    val success: Boolean
)

data class TransactionsInitiateResponseItem(
    val store: Store,
    val transaction_number: String,
    val wallet: Wallet
)

data class Store(
    val name: String,
    val normal_discount_percentage: String,
    val vip_discount_percentage: String
)

data class Transaction(
    val id: Int,
    val transaction_number: String,
    val user_id: Int,
    val store_id: Int,
    val bill_amount: Double,
    val vendor_discount: Double,
    val cashback_used: Double,
    val coupon_discount: Double,
    val final_amount: Double,
    val cashback_earned: Double,
    val payment_method: String,
    val payment_status: String,
    val comment: String?,
    val created_at: String,
    val updated_at: String
)


data class MyTransactionListResponse(
    val data: List<MyTransactionListItem>,
    val pagination: Pagination,
    val success: Boolean
)

data class  MyTransactionListItem(
    val bill_amount: String,
    val cashback_earned: String,
    val cashback_used: String,
    val category_id: Int,
    val category_name: String,
    val comment: String,
    val coupon_discount: String,
    val coupon_id: String,
    val created_at: String,
    val error_msg: Any,
    val final_amount: String,
    val id: Int,
    val payment_method: String,
    val payment_status: String,
    val store_id: Int,
    val store_logo: Any,
    val store_name: String,
    val transaction_number: String,
    val updated_at: String,
    val user_id: Int,
    val vendor_discount: String
)

data class TransactionStatsResponse(
    val success: Boolean,
    val data: TransactionStatsData,
)

data class TransactionStatsData(
    val total_transactions: String,
    val total_bill_amount: String,
    val total_paid_amount: String,
    val total_saved: String,
    val total_cashback_earned: String,
    val total_cashback_used: String,
    val total_vendor_discount: String,
    val total_coupon_discount: String,
    val avg_transaction_amount: String,
)

