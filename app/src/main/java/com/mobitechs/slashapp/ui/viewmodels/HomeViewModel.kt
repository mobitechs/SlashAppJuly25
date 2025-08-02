package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.CategoryItem
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.model.User
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Home UI State
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val categories: List<CategoryItem> = emptyList(),
    val stores: List<StoreListItem> = emptyList(),
    val isCategoriesLoading: Boolean = false,
    val isStoresLoading: Boolean = false,
    val categoriesError: String = "",
    val storesError: String = "",
    // User wallet data
    val isUserDataLoading: Boolean = false,
    val userDataError: String = "",
    val user: User? = null,
    val availableCashback: String = "₹0",
    val totalEarned: String = "₹0"
)

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var hasInitialized = false

    fun initializeData() {
        if (!hasInitialized) {
            hasInitialized = true
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        loadUserData()
        loadCategories()
        loadTopStores()
    }

    private fun loadUserData() {
        _uiState.update { it.copy(isUserDataLoading = true, userDataError = "") }

        viewModelScope.launch {
            try {
                val profileResponse = authRepository.getUserDetails()
                if (profileResponse.success) {
                    val user = profileResponse.data

                    // Update UI state with wallet data
                    _uiState.update {
                        it.copy(
                            isUserDataLoading = false,
                            userDataError = "",
                            user = user,
                            availableCashback = "₹${user.wallet.available_cashback}",
                            totalEarned = "₹${user.wallet.total_earned}"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUserDataLoading = false,
                            userDataError = "Failed to load wallet details: ${profileResponse.message}"
                        )
                    }
                    showToast("Failed to load wallet details: ${profileResponse.message}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUserDataLoading = false,
                        userDataError = "Failed to load wallet details: ${e.message}"
                    )
                }
                showToast("Failed to load wallet details: ${e.message}")
            }
        }
    }

    fun loadCategories() {
        _uiState.update { it.copy(isCategoriesLoading = true, categoriesError = "") }

        viewModelScope.launch {
            try {
                val response = homeRepository.getCategoryList()

                if (response.success) {
                    // Filter active categories and sort by display_order
                    val activeCategories = response.data
                        .filter { it.is_active == 1 }
                        .sortedBy { it.display_order }

                    _uiState.update {
                        it.copy(
                            categories = activeCategories,
                            isCategoriesLoading = false,
                            categoriesError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isCategoriesLoading = false,
                            categoriesError = "Failed to load categories"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCategoriesLoading = false,
                        categoriesError = e.message ?: "Failed to load categories"
                    )
                }
            }
        }
    }

    fun loadTopStores() {
        _uiState.update { it.copy(isStoresLoading = true, storesError = "") }

        viewModelScope.launch {
            try {
                val response = homeRepository.getTopStoreList()

                if (response.success) {
                    // Filter active stores
                    val activeStores = response.data.filter { it.is_active == 1 }

                    _uiState.update {
                        it.copy(
                            stores = activeStores,
                            isStoresLoading = false,
                            storesError = ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isStoresLoading = false,
                            storesError = "Failed to load stores"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStoresLoading = false,
                        storesError = e.message ?: "Failed to load stores"
                    )
                }
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    fun clearCategoriesError() {
        _uiState.update { it.copy(categoriesError = "") }
    }

    fun clearStoresError() {
        _uiState.update { it.copy(storesError = "") }
    }

    fun clearUserDataError() {
        _uiState.update { it.copy(userDataError = "") }
    }

    // Helper function to get fallback icon resource based on category name
    // Used when server images fail to load or are not available
    fun getFallbackIconRes(categoryName: String): Int {
        return when (categoryName.lowercase()) {
            "food" -> com.mobitechs.slashapp.R.drawable.cat_food
            "grocery" -> com.mobitechs.slashapp.R.drawable.cat_grocery
            "fashion" -> com.mobitechs.slashapp.R.drawable.cat_fashion
            "health" -> com.mobitechs.slashapp.R.drawable.cat_health
            "wellness" -> com.mobitechs.slashapp.R.drawable.cat_health
            "beauty" -> com.mobitechs.slashapp.R.drawable.cat_food // Using food as fallback
            "electronics" -> com.mobitechs.slashapp.R.drawable.cat_fashion // Using fashion as fallback
            "others" -> com.mobitechs.slashapp.R.drawable.cat_food
            else -> com.mobitechs.slashapp.R.drawable.cat_food // Default fallback
        }
    }

    // Helper function to calculate distance (placeholder implementation)
    fun calculateDistance(latitude: String?, longitude: String?): String {
        // TODO: Implement actual distance calculation using user's location
        // For now, return a placeholder distance
        return "${(1..5).random()}.${(0..9).random()} km away"
    }
}