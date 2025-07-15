package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.CouponResponse
import com.mobitechs.slashapp.data.model.CouponValidationResponse
import com.mobitechs.slashapp.data.model.CreateTransactionRequest
import com.mobitechs.slashapp.data.model.SaveTransactionRequest
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.TransactionsInitiateResponse
import com.mobitechs.slashapp.data.model.UpdateTransactionResponse
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

    suspend fun initiateTransaction(saveTransactionRequest: SaveTransactionRequest): TransactionsInitiateResponse = withContext(Dispatchers.IO) {
        val response = apiService.initiateTransaction(saveTransactionRequest)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Coupon Validation failed: ${response.message()}")
        }
    }
  suspend fun updateTransaction(transactionId: String, saveTransactionRequest: SaveTransactionRequest): UpdateTransactionResponse = withContext(Dispatchers.IO) {
        val response = apiService.updateTransaction(transactionId,saveTransactionRequest)

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Coupon Validation failed: ${response.message()}")
        }
    }




}