package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.AddStoreReviewRequest
import com.mobitechs.slashapp.data.model.AddStoreReviewResponse
import com.mobitechs.slashapp.data.model.CategoryListResponse
import com.mobitechs.slashapp.data.model.CouponValidationResponse
import com.mobitechs.slashapp.data.model.MyTransactionListResponse
import com.mobitechs.slashapp.data.model.OTPVerifyResponse
import com.mobitechs.slashapp.data.model.ProfileResponse
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.model.SaveTransactionRequest
import com.mobitechs.slashapp.data.model.SendOTPResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.TransactionStatsResponse
import com.mobitechs.slashapp.data.model.TransactionsInitiateResponse
import com.mobitechs.slashapp.data.model.UpdateTransactionResponse
import com.mobitechs.slashapp.data.model.ValidateCouponRequest
import com.mobitechs.slashapp.data.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth endpoints
    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOTPResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<OTPVerifyResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterUserRequest): Response<OTPVerifyResponse>

    @GET("users/profile")
    suspend fun getUserDetails(): Response<ProfileResponse>

    // Store endpoints------------------------------------------------------------------------------------------
    @GET("stores/categories")
    suspend fun getCategoryList(): Response<CategoryListResponse>

    @GET("stores/top-sequence")
    suspend fun getTopStoreList(): Response<StoreListResponse>

    @GET("stores")
    suspend fun getAllStoreList(
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>


    @GET("stores")
    suspend fun getCategoryWiseStoreList(
        @Query("categoryId") categoryId: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>

    @GET("stores")
    suspend fun getSearchWiseStoreList(
        @Query("query") query: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>


    @GET("stores/search?q=")
    suspend fun getSearchWiseStoreList2( @Query("query") storeId: String): Response<StoreListResponse>

    @GET("stores/favorites")
    suspend fun getFavouriteStoreList(): Response<StoreListResponse>

    @GET("stores/{storeId}")
    suspend fun getStoreDetails(
        @Path("storeId") storeId: String
    ): Response<StoreResponse>

    @POST("stores/{storeId}}/reviews")
    suspend fun addStoreReview( @Path("storeId") storeId: String,@Body request: AddStoreReviewRequest): Response<AddStoreReviewResponse>


    @POST("coupons/validateCoupon")
    suspend fun validateCoupon(@Body request: ValidateCouponRequest): Response<CouponValidationResponse>


    @POST("transactions/initiate")
    suspend fun initiateTransaction(@Body request: SaveTransactionRequest): Response<TransactionsInitiateResponse>

    @POST("transactions/{transactionId}/complete")
    suspend fun updateTransaction(
        @Path("transactionId") transactionId: String,
        @Body request: SaveTransactionRequest
    ): Response<UpdateTransactionResponse>



    @GET("transactions")
    suspend fun getMyTransactionList(
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<MyTransactionListResponse>


    @GET("transactions/stats")
    suspend fun getTransactionStats(): Response<TransactionStatsResponse>



}