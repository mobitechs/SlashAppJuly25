package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.model.StoreReviewsListItem
import com.mobitechs.slashapp.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreDetailsUiState(
    val store: StoreListItem? = null,
    val isStoreLoading: Boolean = false,
    val storeError: String = "",

    val reviews: List<StoreReviewsListItem> = emptyList(),
    val isReviewsLoading: Boolean = false,
    val reviewsError: String = "",
    val hasMoreReviews: Boolean = true,
    val isLoadingMoreReviews: Boolean = false,
    val currentReviewsPage: Int = 1,

    val isFavorite: Boolean = false,
    val isUpdatingFavorite: Boolean = false,

    val isRefreshing: Boolean = false,

    val helpfulReviews: Map<Int, Boolean> = emptyMap(), // reviewId -> isMarked
    val reportedReviews: Map<Int, Boolean> = emptyMap(), // reviewId -> isMarked
    val markingInProgress: Set<Int> = emptySet() // reviewIds being marked

)

class StoreDetailsViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(StoreDetailsUiState())
    val uiState: StateFlow<StoreDetailsUiState> = _uiState.asStateFlow()

    private var storeId: String = ""
    private val reviewsPageSize = 10

    fun loadStoreDetails(id: String) {
        storeId = id
        loadStore()
        loadReviews(page = 1, isRefresh = true)
    }

    private fun loadStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isStoreLoading = true,
                storeError = ""
            )

            try {
                val response = storeRepository.getStoreWiseDetails(storeId)
                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        store = response.data,
                        isStoreLoading = false,
                        isFavorite = response.data?.is_favourite == true // Assuming is_partner indicates favorite
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isStoreLoading = false,
                        storeError = response.message ?: "Failed to load store details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStoreLoading = false,
                    storeError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun loadReviews(page: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(
                    isReviewsLoading = true,
                    reviewsError = ""
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingMoreReviews = true
                )
            }

            try {
                val response = storeRepository.getStoreReviews(
                    storeId = storeId,
                    page = page.toString(),
                    limit = reviewsPageSize.toString()
                )

                if (response.success) {
                    val newReviews = if (isRefresh) {
                        response.data
                    } else {
                        _uiState.value.reviews + response.data
                    }

                    _uiState.value = _uiState.value.copy(
                        reviews = newReviews,
                        isReviewsLoading = false,
                        isLoadingMoreReviews = false,
                        hasMoreReviews = response.data.size >= reviewsPageSize,
                        currentReviewsPage = page,
                        reviewsError = ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isReviewsLoading = false,
                        isLoadingMoreReviews = false,
                        reviewsError = response.message ?: "Failed to load reviews"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReviewsLoading = false,
                    isLoadingMoreReviews = false,
                    reviewsError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun loadMoreReviews() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMoreReviews && currentState.hasMoreReviews) {
            loadReviews(currentState.currentReviewsPage + 1)
        }
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState.isUpdatingFavorite || currentState.store == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingFavorite = true)

            try {
                val response = storeRepository.addToFavourites(storeId)

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isFavorite = !currentState.isFavorite,
                        isUpdatingFavorite = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingFavorite = false
                    )
                    // Could show a toast or error message here
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingFavorite = false
                )
                // Could show a toast or error message here
            }
        }
    }

    fun markReviewHelpful(reviewId: Int) {
        val currentState = _uiState.value
        if (currentState.markingInProgress.contains(reviewId)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                markingInProgress = _uiState.value.markingInProgress + reviewId
            )

            try {
                val response = storeRepository.addRemoveHelpfulReview(reviewId.toString())

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        helpfulReviews = _uiState.value.helpfulReviews.toMutableMap().apply {
                            put(reviewId, response.data.is_marked)
                        },
                        markingInProgress = _uiState.value.markingInProgress - reviewId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        markingInProgress = _uiState.value.markingInProgress - reviewId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    markingInProgress = _uiState.value.markingInProgress - reviewId
                )
            }
        }
    }

    fun markReviewReport(reviewId: Int) {
        val currentState = _uiState.value
        if (currentState.markingInProgress.contains(reviewId)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                markingInProgress = _uiState.value.markingInProgress + reviewId
            )

            try {
                val response = storeRepository.addRemoveReportReview(reviewId.toString())

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        reportedReviews = _uiState.value.reportedReviews.toMutableMap().apply {
                            put(reviewId, response.data.is_marked)
                        },
                        markingInProgress = _uiState.value.markingInProgress - reviewId
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        markingInProgress = _uiState.value.markingInProgress - reviewId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    markingInProgress = _uiState.value.markingInProgress - reviewId
                )
            }
        }
    }


    fun refreshData() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)

        viewModelScope.launch {
            loadStore()
            loadReviews(page = 1, isRefresh = true)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }



    fun retryLoadStore() {
        loadStore()
    }

    fun retryLoadReviews() {
        loadReviews(page = 1, isRefresh = true)
    }


    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            storeError = "",
            reviewsError = ""
        )
    }
}