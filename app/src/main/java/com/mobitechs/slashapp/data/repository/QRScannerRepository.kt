package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.CouponResponse
import com.mobitechs.slashapp.data.model.CouponValidationResponse
import com.mobitechs.slashapp.data.model.CreateTransactionRequest
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.TransactionsInitiateResponse
import com.mobitechs.slashapp.data.model.ValidateCouponRequest
import com.mobitechs.slashapp.data.model.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QRScannerRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    suspend fun getStoreDetails(storeId: Int): StoreResponse = withContext(Dispatchers.IO) {
        val response = apiService.getStoreDetails(storeId.toString())

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Failed to get store details: ${response.message()}")
        }
    }

    suspend fun validateCoupon(code: String, storeId: Int, billAmount: String): CouponValidationResponse = withContext(Dispatchers.IO) {
        val request = ValidateCouponRequest(code = code, store_id = storeId, bill_amount = billAmount)
        val response = apiService.validateCoupon(request)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Coupon Validation failed: ${response.message()}")
        }
    }

    suspend fun createTransaction(
        storeId: Int,
        billAmount: Double,
        vendorDiscount: Double,
        cashbackUsed: Double,
        couponCode: String?,
        couponDiscount: Double,
        finalAmount: Double,
        paymentMethod: String,
        comment: String?
    ): TransactionsInitiateResponse = withContext(Dispatchers.IO) {
        val request = CreateTransactionRequest(
            user_id = sharedPrefsManager.getUser()!!.id,
            store_id = storeId,
            bill_amount = billAmount,
            vendor_discount = vendorDiscount,
            cashback_used = cashbackUsed,
            coupon_code = couponCode,
            coupon_discount = couponDiscount,
            final_amount = finalAmount,
            payment_method = paymentMethod,
            comment = comment
        )

        val response = apiService.createTransaction(request)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Send OTP failed: ${response.message()}")
        }
    }


}