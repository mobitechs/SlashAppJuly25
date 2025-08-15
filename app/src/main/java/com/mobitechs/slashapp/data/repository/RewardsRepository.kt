package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.CouponResponse
import com.mobitechs.slashapp.data.model.RewardCouponResponse
import com.mobitechs.slashapp.data.model.RewardHistoryResponse
import com.mobitechs.slashapp.data.model.RewardSummeryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RewardsRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    suspend fun getRewardStats(): RewardSummeryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getRewardStats()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }

    suspend fun getAvailableCoupons(): RewardCouponResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getAvailableCoupons()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get available coupons: ${response.message()}")
            }
        }

    suspend fun getRewardHistory(page: String = "1", limit: String = "100"): RewardHistoryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getRewardHistory(page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward history: ${response.message()}")
            }
        }


    suspend fun getRewardHistoryWithFilter(
        rewardType: String,
        page: String,
        limit: String
    ): RewardHistoryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getRewardHistoryWithFilter(rewardType, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get filtered reward history: ${response.message()}")
            }
        }

    suspend fun getRewardHistoryWithDateRange(
        dateFrom: String,
        dateTo: String,
        page: String,
        limit: String
    ): RewardHistoryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getRewardHistoryWithDateRange(dateFrom, dateTo, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward history by date range: ${response.message()}")
            }
        }
}