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


