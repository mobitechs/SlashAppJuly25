package com.mobitechs.slashapp.data.repository

import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.model.CategoryListResponse
import com.mobitechs.slashapp.data.model.StoreListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class HomeRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {


    suspend fun getCategoryList(): CategoryListResponse = withContext(Dispatchers.IO) {
        val response = apiService.getCategoryList()

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Failed to get store details: ${response.message()}")
        }
    }

    suspend fun getTopStoreList(): StoreListResponse = withContext(Dispatchers.IO) {
        val response = apiService.getTopStoreList()

        if (response.isSuccessful) {
            val apiResponse = response.body() ?: throw Exception("Empty response body")
            return@withContext apiResponse
        } else {
            throw Exception("Failed to get store details: ${response.message()}")
        }
    }


}