package com.mobitechs.slashapp.data.api

import com.mobitechs.slashapp.data.model.AddStoreReviewRequest
import com.mobitechs.slashapp.data.model.AddStoreReviewResponse
import com.mobitechs.slashapp.data.model.CategoryListResponse
import com.mobitechs.slashapp.data.model.CouponResponse
import com.mobitechs.slashapp.data.model.CouponValidationResponse
import com.mobitechs.slashapp.data.model.MyTransactionListResponse
import com.mobitechs.slashapp.data.model.OTPVerifyResponse
import com.mobitechs.slashapp.data.model.ProfileResponse
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.model.ReviewMarkResponse
import com.mobitechs.slashapp.data.model.RewardCouponResponse
import com.mobitechs.slashapp.data.model.RewardHistoryResponse
import com.mobitechs.slashapp.data.model.RewardSummeryResponse
import com.mobitechs.slashapp.data.model.SaveTransactionRequest
import com.mobitechs.slashapp.data.model.SendOTPResponse
import com.mobitechs.slashapp.data.model.SendOtpRequest
import com.mobitechs.slashapp.data.model.SpinWheelCampaignDetailsResponse
import com.mobitechs.slashapp.data.model.SpinWheelCampaignResponse
import com.mobitechs.slashapp.data.model.SpinWheelHistoryResponse
import com.mobitechs.slashapp.data.model.SpinWheelResultResponse
import com.mobitechs.slashapp.data.model.SpinWheelSummeryResponse
import com.mobitechs.slashapp.data.model.StoreListResponse
import com.mobitechs.slashapp.data.model.StoreResponse
import com.mobitechs.slashapp.data.model.StoreReviewsListResponse
import com.mobitechs.slashapp.data.model.TransactionStatsResponse
import com.mobitechs.slashapp.data.model.TransactionsInitiateResponse
import com.mobitechs.slashapp.data.model.UpdateTransactionResponse
import com.mobitechs.slashapp.data.model.ValidateCouponRequest
import com.mobitechs.slashapp.data.model.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
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


    @GET("stores/categories/{categoryId}/stores")
    suspend fun getCategoryWiseStoreList(
        @Path("categoryId") categoryId: String,  // ← Changed from @Query to @Path
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>


    @GET("stores")
    suspend fun getSearchWiseStoreList(
        @Query("search") query: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreListResponse>


    @GET("stores/search?q=")
    suspend fun getSearchWiseStoreList2( @Query("search") storeId: String): Response<StoreListResponse>

    @GET("stores/favorites")
    suspend fun getFavouriteStoreList(): Response<StoreListResponse>

    @GET("stores/{storeId}")
    suspend fun getStoreWiseDetails(
        @Path("storeId") storeId: String
    ): Response<StoreResponse>

    @GET("stores/{storeId}/reviews")
    suspend fun getStoreReviews(
        @Path("storeId") query: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<StoreReviewsListResponse>

    @POST("stores/{storeId}}/reviews")
    suspend fun addStoreReview( @Path("storeId") storeId: String,@Body request: AddStoreReviewRequest): Response<AddStoreReviewResponse>

    @POST("stores/{storeId}/favorite")
    suspend fun addRemoveToFavourites(
        @Path("storeId") storeId: String
    ): Response<StoreResponse>

    @POST("stores/reviews/{reviewId}/helpful")
    suspend fun addRemoveHelpfulReview(
        @Path("reviewId") storeId: String
    ): Response<ReviewMarkResponse>


    @POST("stores/reviews/{reviewId}/report")
    suspend fun addRemoveReportReview(
        @Path("reviewId") storeId: String
    ): Response<ReviewMarkResponse>



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

    @GET("transactions")
    suspend fun getMyTransactionListWithSearchFilter(
        @Query("search") search: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<MyTransactionListResponse>

    @GET("transactions")
    suspend fun getMyTransactionListWithDateRangeFilter(
        @Query("date_from") date_from: String,
        @Query("date_to") date_to: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<MyTransactionListResponse>

    @GET("transactions")
    suspend fun getMyTransactionListWithAmountFilter(
        @Query("min_amount") min_amount: String,
        @Query("max_amount") max_amount: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<MyTransactionListResponse>

    @GET("transactions")
    suspend fun getMyTransactionListWithStoreAndStatusFilter(
        @Query("store_id") store_id: String,
        @Query("status") status: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<MyTransactionListResponse>

    @GET("transactions/stats")
    suspend fun getTransactionStats(): Response<TransactionStatsResponse>




//reward

    @GET("rewards/summary")
    suspend fun getRewardStats(): Response<RewardSummeryResponse>

    @GET("rewards/coupons/available")
    suspend fun getAvailableCoupons(): Response<RewardCouponResponse>

    @GET("rewards/history")
    suspend fun getRewardHistory(
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<RewardHistoryResponse>

    @GET("rewards/history/filter")
    suspend fun getRewardHistoryWithFilter(
        @Query("reward_type") rewardType: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<RewardHistoryResponse>

    @GET("rewards/history/date-range")
    suspend fun getRewardHistoryWithDateRange(
        @Query("date_from") dateFrom: String,
        @Query("date_to") dateTo: String,
        @Query("page") page: String,
        @Query("limit") limit: String
    ): Response<RewardHistoryResponse>



// daily spin the wheel

    @GET("daily-rewards/summary")
    suspend fun getDailySpinWheelSummery(): Response<SpinWheelSummeryResponse>


    @GET("daily-rewards/campaigns")
    suspend fun getDailySpinWheelCampaign(): Response<SpinWheelCampaignResponse>

    @GET("daily-rewards/spin-wheel/campaignId")
    suspend fun getDailySpinWheelCampaignDetails(campaignId: String): Response<SpinWheelCampaignDetailsResponse>


    @POST("daily-rewards/spin-wheel/{campaignId}/spin")
    suspend fun spinWheelResult(
        @Path("campaignId") campaignId: String,
    ): Response<SpinWheelResultResponse>

    @GET("daily-rewards/history")
    suspend fun getSpinWheelHistory(): Response<SpinWheelHistoryResponse>



}