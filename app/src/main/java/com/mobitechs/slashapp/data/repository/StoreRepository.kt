package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {


    suspend fun getAllStoreList(page: String, limit: String): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getAllStoreList(page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get store details: ${response.message()}")
            }
        }

    suspend fun getCategoryWiseStoreList(categoryId: String,page: String, limit: String): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getCategoryWiseStoreList(categoryId, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get category wise store details: ${response.message()}")
            }
        }

    suspend fun getSearchWiseStoreList(query: String,page: String, limit: String): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getSearchWiseStoreList(query, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

    suspend fun getFavouriteStoreList(): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getFavouriteStoreList()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

    suspend fun getStoreDetails(storeId: String): StoreResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getStoreDetails(storeId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

    suspend fun addStoreReview(storeId: String): StoreResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getStoreDetails(storeId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

}