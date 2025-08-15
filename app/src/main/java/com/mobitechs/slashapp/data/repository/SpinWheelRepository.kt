package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.SpinWheelCampaignDetailsResponse
import com.mobitechs.slashapp.data.model.SpinWheelCampaignResponse
import com.mobitechs.slashapp.data.model.SpinWheelHistoryResponse
import com.mobitechs.slashapp.data.model.SpinWheelResultResponse
import com.mobitechs.slashapp.data.model.SpinWheelSummeryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpinWheelRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    suspend fun getDailySpinWheelSummery(): SpinWheelSummeryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getDailySpinWheelSummery()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }

    suspend fun getDailySpinWheelCampaign(): SpinWheelCampaignResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getDailySpinWheelCampaign()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }

    suspend fun getDailySpinWheelCampaignDetails(campaignId: String): SpinWheelCampaignDetailsResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getDailySpinWheelCampaignDetails(campaignId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }

    suspend fun spinWheelResult(campaignId: String): SpinWheelResultResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.spinWheelResult(campaignId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }

    suspend fun getSpinWheelHistory(): SpinWheelHistoryResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getSpinWheelHistory()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get reward stats: ${response.message()}")
            }
        }




}