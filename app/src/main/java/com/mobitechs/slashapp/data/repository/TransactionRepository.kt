package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.MyTransactionListResponse
import com.mobitechs.slashapp.data.model.TransactionStatsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TransactionRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {


    suspend fun getMyTransactionList(page: String, limit: String): MyTransactionListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getMyTransactionList(page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction details: ${response.message()}")
            }
        }

    suspend fun getMyTransactionListWithSearchFilter(
        search: String,
        page: String,
        limit: String
    ): MyTransactionListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getMyTransactionListWithSearchFilter(search, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction details: ${response.message()}")
            }
        }

    suspend fun getMyTransactionListWithDateRangeFilter(
        date_from: String,
        date_to: String,
        page: String,
        limit: String
    ): MyTransactionListResponse =
        withContext(Dispatchers.IO) {
            val response =
                apiService.getMyTransactionListWithDateRangeFilter(date_from, date_to, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction details: ${response.message()}")
            }
        }

    suspend fun getMyTransactionListWithAmountFilter(
        min_amount: String,
        max_amount: String,
        page: String,
        limit: String
    ): MyTransactionListResponse =
        withContext(Dispatchers.IO) {
            val response =
                apiService.getMyTransactionListWithAmountFilter(min_amount, max_amount, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction details: ${response.message()}")
            }
        }

    suspend fun getMyTransactionListWithStoreAndStatusFilter(
        store_id: String,
        status: String,
        page: String,
        limit: String
    ): MyTransactionListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getMyTransactionListWithStoreAndStatusFilter(
                store_id,
                status,
                page,
                limit
            )

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction details: ${response.message()}")
            }
        }


    suspend fun getTransactionStats(): TransactionStatsResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getTransactionStats()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get transaction stats details: ${response.message()}")
            }
        }


}