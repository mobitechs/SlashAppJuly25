package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.CategoryListResponse
import com.mobitechs.slashapp.data.model.CouponResponse
import com.mobitechs.slashapp.data.model.CreateTransactionRequest
import com.mobitechs.slashapp.data.model.OTPVerifyResponse
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.model.SendOTPResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.TransactionsInitiateResponse
import com.mobitechs.slashapp.data.model.ValidateCouponRequest
import com.mobitechs.slashapp.data.model.VerifyOtpRequest
import com.mobitechs.slashapp.data.model.Wallet
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

    // Store endpoints
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


    @POST("coupon/validate")
    suspend fun validateCoupon(@Body request: ValidateCouponRequest): Response<CouponResponse>

    @POST("transaction/create")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): Response<TransactionsInitiateResponse>

    @GET("user/wallet")
    suspend fun getUserWallet(): Response<Wallet>
}