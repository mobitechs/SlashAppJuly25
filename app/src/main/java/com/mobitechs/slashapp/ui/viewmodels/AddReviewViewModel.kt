package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.model.StoreReviewsListItem
import com.mobitechs.slashapp.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddReviewUiState(
    // Store info
    val store: StoreListItem? = null,
    val isStoreLoading: Boolean = false,
    val storeError: String = "",

    // Favorite state
    val isFavorite: Boolean = false,
    val isUpdatingFavorite: Boolean = false,

    // Review form state
    val selectedRating: Int = 0,
    val reviewTitle: String = "",
    val reviewDescription: String = "",

    // Submission state
    val isSubmitting: Boolean = false,
    val submissionError: String = "",
    val submissionSuccess: Boolean = false,

    // Review being edited (for update mode)
    val existingReview: StoreReviewsListItem? = null,
    val isUpdateMode: Boolean = false
)

class AddReviewViewModel(
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddReviewUiState())
    val uiState: StateFlow<AddReviewUiState> = _uiState.asStateFlow()

    private var storeId: String = ""

    fun initialize(storeId: String, existingReview: StoreReviewsListItem? = null) {
        this.storeId = storeId

        // Set up initial state based on whether we're updating or adding
        _uiState.value = _uiState.value.copy(
            existingReview = existingReview,
            isUpdateMode = existingReview != null,
            selectedRating = existingReview?.rating ?: 0,
            reviewTitle = existingReview?.title ?: "",
            reviewDescription = existingReview?.description ?: ""
        )

        loadStoreDetails()
    }

    private fun loadStoreDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isStoreLoading = true,
                storeError = ""
            )

            try {
                val response = storeRepository.getStoreWiseDetails(storeId)
                if (response.success) {
                    val store = response.data

                    // Check if user has already reviewed and we're not in explicit update mode
                    val userReview = store?.user_review
                    val shouldUpdateMode = userReview != null

                    _uiState.value = _uiState.value.copy(
                        store = store,
                        isStoreLoading = false,
                        isFavorite = store?.is_favourite == true, // Use is_favourite field
                        // If user has already reviewed, switch to update mode
                        isUpdateMode = shouldUpdateMode,
                        selectedRating = userReview?.rating ?: _uiState.value.selectedRating,
                        reviewTitle = userReview?.title ?: _uiState.value.reviewTitle,
                        reviewDescription = userReview?.description ?: _uiState.value.reviewDescription
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

    fun updateRating(rating: Int) {
        _uiState.value = _uiState.value.copy(selectedRating = rating)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(reviewTitle = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(reviewDescription = description)
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingFavorite = false
                )
            }
        }
    }

    fun submitReview() {
        val currentState = _uiState.value

        // Validation
        if (currentState.selectedRating == 0) {
            _uiState.value = _uiState.value.copy(
                submissionError = "Please select a rating"
            )
            return
        }

        if (currentState.reviewTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(
                submissionError = "Please enter a review title"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                submissionError = "",
                submissionSuccess = false
            )

            try {
                val response = if (currentState.isUpdateMode) {
                    // For update, you might need a different API endpoint
                    // This depends on your backend implementation
                    storeRepository.addStoreReview(
                        storeId = storeId,
                        rating = currentState.selectedRating.toString(),
                        title = currentState.reviewTitle,
                        description = currentState.reviewDescription
                    )
                } else {
                    // Add new review
                    storeRepository.addStoreReview(
                        storeId = storeId,
                        rating = currentState.selectedRating.toString(),
                        title = currentState.reviewTitle,
                        description = currentState.reviewDescription
                    )
                }

                if (response.success) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        submissionError = response.message ?: "Failed to submit review"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submissionError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun retryLoadStore() {
        loadStoreDetails()
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            storeError = "",
            submissionError = ""
        )
    }

    fun resetSubmissionState() {
        _uiState.value = _uiState.value.copy(
            submissionSuccess = false,
            submissionError = ""
        )
    }

    // Validation helpers
    fun isFormValid(): Boolean {
        val currentState = _uiState.value
        return currentState.selectedRating > 0 && currentState.reviewTitle.isNotBlank()
    }

    fun getSubmitButtonText(): String {
        return if (_uiState.value.isUpdateMode) "Update" else "Submit"
    }

    fun getScreenTitle(): String {
        return if (_uiState.value.isUpdateMode) "Update Your Review" else "Add Your Review"
    }

    fun getUserName(): String? {
        return _uiState.value.existingReview?.let { "${it.first_name} ${it.last_name}" }
            ?: if (_uiState.value.store?.user_review != null) "Your Review" else null
    }
}