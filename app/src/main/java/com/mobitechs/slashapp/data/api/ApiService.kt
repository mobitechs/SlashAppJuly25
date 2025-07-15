package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.CategoryListResponse
import com.mobitechs.slashapp.data.model.CouponValidationResponse
import com.mobitechs.slashapp.data.model.OTPVerifyResponse
import com.mobitechs.slashapp.data.model.ProfileResponse
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.model.SaveTransactionRequest
import com.mobitechs.slashapp.data.model.SendOTPResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
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
    @GET("store/categories")
    suspend fun getCategoryList(): Response<CategoryListResponse>

    @GET("store/topStore")
    suspend fun getTopStoreList(): Response<StoreListResponse>

    @GET("stores")
    suspend fun getStoreList(
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>

    @GET("store/categoryWiseStore/{categoryId}")
    suspend fun getCategoryWiseStoreList(
        @Path("categoryId") categoryId: String
    ): Response<StoreListResponse>

    @GET("stores/{storeId}")
    suspend fun getStoreDetails(
        @Path("storeId") storeId: String
    ): Response<StoreResponse>


    @POST("coupons/validateCoupon")
    suspend fun validateCoupon(@Body request: ValidateCouponRequest): Response<CouponValidationResponse>


    @POST("transactions/initiate")
    suspend fun initiateTransaction(@Body request: SaveTransactionRequest): Response<TransactionsInitiateResponse>

    @POST("transactions/{transactionId}/complete")
    suspend fun updateTransaction(
        @Path("transactionId") transactionId: String,
        @Body request: SaveTransactionRequest
    ): Response<UpdateTransactionResponse>


}