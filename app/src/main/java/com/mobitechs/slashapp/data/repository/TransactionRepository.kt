package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.MyTransactionListResponse
import com.mobitechs.slashapp.data.model.StoreListResponse
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