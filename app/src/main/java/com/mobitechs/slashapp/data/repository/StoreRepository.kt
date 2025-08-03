package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.AddStoreReviewRequest
import com.mobitechs.slashapp.data.model.AddStoreReviewResponse
import com.mobitechs.slashapp.data.model.ReviewMarkResponse
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.StoreReviewsListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    /**
     * Get all stores with pagination
     */
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

    /**
     * Get stores by category with pagination
     */
    suspend fun getCategoryWiseStoreList(categoryId: String, page: String, limit: String): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = if (categoryId.isEmpty() || categoryId == "0") {
                // If categoryId is empty or "0" (All category), get all stores
                apiService.getAllStoreList(page, limit)
            } else {
                apiService.getCategoryWiseStoreList(categoryId, page, limit)
            }

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get category wise store details: ${response.message()}")
            }
        }

    /**
     * Search stores with pagination
     */
    suspend fun getSearchWiseStoreList(query: String, page: String, limit: String): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getSearchWiseStoreList(query, page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

    /**
     * Get store details by ID
     */
    suspend fun getStoreWiseDetails(storeId: String): StoreResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getStoreWiseDetails(storeId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get searched store details: ${response.message()}")
            }
        }

    suspend fun getStoreReviews(storeId: String,page: String, limit: String): StoreReviewsListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getStoreReviews(storeId,page, limit)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get store details: ${response.message()}")
            }
        }

    /**
     * Get user's favourite stores
     */
    suspend fun getFavouriteStoreList(): StoreListResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.getFavouriteStoreList()

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to get favourite stores: ${response.message()}")
            }
        }



    /**
     * Add or update store review
     */
    suspend fun addStoreReview(
        storeId: String,
        rating: String,
        title: String,
        description: String
    ): AddStoreReviewResponse =
        withContext(Dispatchers.IO) {
            // You'll need to implement this API call based on your backend
            var req = AddStoreReviewRequest(rating, title, description)
            val response = apiService.addStoreReview(storeId,req)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to add store review: ${response.message()}")
            }
        }

    /**
     * Add store to favourites
     */
    suspend fun addToFavourites(storeId: String): StoreResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.addRemoveToFavourites(storeId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to add to favourites: ${response.message()}")
            }
        }

    /**
     * Add or Remove helpful marking of review
     */
    suspend fun addRemoveHelpfulReview(reviewId: String): ReviewMarkResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.addRemoveHelpfulReview(reviewId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to mark helpful: ${response.message()}")
            }
        }

    /**
     * Add or Remove report marking of review
     */
    suspend fun addRemoveReportReview(reviewId: String): ReviewMarkResponse =
        withContext(Dispatchers.IO) {
            val response = apiService.addRemoveReportReview(reviewId)

            if (response.isSuccessful) {
                val apiResponse = response.body() ?: throw Exception("Empty response body")
                return@withContext apiResponse
            } else {
                throw Exception("Failed to mark report: ${response.message()}")
            }
        }
}