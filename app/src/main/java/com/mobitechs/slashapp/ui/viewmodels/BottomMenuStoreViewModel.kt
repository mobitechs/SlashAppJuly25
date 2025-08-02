package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.CategoryItem
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.repository.HomeRepository
import com.mobitechs.slashapp.data.repository.StoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Store List UI State
data class StoreListUiState(
    // Categories
    val categories: List<CategoryItem> = emptyList(),
    val isCategoriesLoading: Boolean = false,
    val categoriesError: String = "",

    // Selected category
    val selectedCategory: CategoryItem? = null,

    // Search
    val searchQuery: String = "",
    val isSearching: Boolean = false,

    // Stores
    val stores: List<StoreListItem> = emptyList(),
    val isStoresLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val storesError: String = "",
    val hasMorePages: Boolean = true,
    val currentPage: Int = 1,

    // General states
    val isRefreshing: Boolean = false
)

class BottomMenuStoreViewModel(
    private val homeRepository: HomeRepository,
    private val storeRepository: StoreRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(StoreListUiState())
    val uiState: StateFlow<StoreListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val pageLimit = 10

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadCategories()
    }

    private fun loadCategories() {
        _uiState.update { it.copy(isCategoriesLoading = true, categoriesError = "") }

        viewModelScope.launch {
            try {
                val response = homeRepository.getCategoryList()

                if (response.success) {
                    // Filter active categories and sort by display_order
                    val activeCategories = response.data
                        .filter { it.is_active == 1 }
                        .sortedBy { it.display_order }

                    // Add "All" category at the beginning
                    val allCategory = CategoryItem(
                        id = "0",
                        name = "All",
                        icon = "",
                        is_active = 1,
                        display_order = 0,
                        created_at = "",
                        updated_at = ""
                    )

                    val categoriesWithAll = listOf(allCategory) + activeCategories

                    _uiState.update {
                        it.copy(
                            categories = categoriesWithAll,
                            selectedCategory = allCategory,
                            isCategoriesLoading = false,
                            categoriesError = ""
                        )
                    }

                    // Load stores for "All" category by default
                    loadStoresByCategory(allCategory, isInitialLoad = true)
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

    fun selectCategory(category: CategoryItem) {
        val currentState = _uiState.value
        if (currentState.selectedCategory?.id != category.id) {
            // Cancel any ongoing search
            searchJob?.cancel()

            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    searchQuery = "",
                    stores = emptyList(), // Clear stores immediately
                    currentPage = 1,
                    hasMorePages = true,
                    storesError = "",
                    isStoresLoading = true // Show loading immediately
                )
            }
            loadStoresByCategory(category, isInitialLoad = true)
        }
    }

    private fun loadStoresByCategory(category: CategoryItem, isInitialLoad: Boolean = false) {
        if (!isInitialLoad) {
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val page = if (isInitialLoad) 1 else currentState.currentPage

                val response = storeRepository.getCategoryWiseStoreList(
                    categoryId = if (category.id == "0") "" else category.id,
                    page = page.toString(),
                    limit = pageLimit.toString()
                )

                if (response.success) {
                    val newStores = response.data.filter { it.is_active == 1 }

                    // FIXED: Always use fresh state and ensure proper replacement for initial load
                    _uiState.update { latestState ->
                        val updatedStores = if (isInitialLoad) {
                            newStores // Always replace completely for initial load
                        } else {
                            latestState.stores + newStores // Append for pagination
                        }

                        latestState.copy(
                            stores = updatedStores,
                            isStoresLoading = false,
                            isLoadingMore = false,
                            storesError = "",
                            currentPage = page + 1,
                            hasMorePages = newStores.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isStoresLoading = false,
                            isLoadingMore = false,
                            storesError = response.message ?: "Failed to load stores"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStoresLoading = false,
                        isLoadingMore = false,
                        storesError = e.message ?: "Failed to load stores"
                    )
                }
            }
        }
    }

    fun loadMoreStores() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMore && currentState.hasMorePages && currentState.searchQuery.isEmpty()) {
            currentState.selectedCategory?.let { category ->
                loadStoresByCategory(category, isInitialLoad = false)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            // If search is cleared, reload category-based stores
            _uiState.value.selectedCategory?.let { category ->
                _uiState.update {
                    it.copy(
                        stores = emptyList(),
                        currentPage = 1,
                        hasMorePages = true,
                        storesError = "",
                        isStoresLoading = true
                    )
                }
                loadStoresByCategory(category, isInitialLoad = true)
            }
        } else {
            // Debounce search
            searchJob = viewModelScope.launch {
                delay(500) // 500ms debounce
                searchStores(query)
            }
        }
    }

    private fun searchStores(query: String) {
        _uiState.update {
            it.copy(
                isSearching = true,
                isStoresLoading = true,
                stores = emptyList(),
                storesError = "",
                currentPage = 1,
                hasMorePages = true
            )
        }

        viewModelScope.launch {
            try {
                val response = storeRepository.getSearchWiseStoreList(query, "1", pageLimit.toString())

                if (response.success) {
                    val stores = response.data.filter { it.is_active == 1 }
                    _uiState.update {
                        it.copy(
                            stores = stores,
                            isStoresLoading = false,
                            isSearching = false,
                            storesError = "",
                            currentPage = 2,
                            hasMorePages = stores.size >= pageLimit
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isStoresLoading = false,
                            isSearching = false,
                            storesError = response.message ?: "Failed to search stores"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStoresLoading = false,
                        isSearching = false,
                        storesError = e.message ?: "Failed to search stores"
                    )
                }
            }
        }
    }

    fun loadMoreSearchResults() {
        val currentState = _uiState.value
        if (!currentState.isLoadingMore && currentState.hasMorePages && currentState.searchQuery.isNotEmpty()) {
            _uiState.update { it.copy(isLoadingMore = true) }

            viewModelScope.launch {
                try {
                    val response = storeRepository.getSearchWiseStoreList(
                        currentState.searchQuery,
                        currentState.currentPage.toString(),
                        pageLimit.toString()
                    )

                    if (response.success) {
                        val newStores = response.data.filter { it.is_active == 1 }
                        _uiState.update { latestState ->
                            latestState.copy(
                                stores = latestState.stores + newStores,
                                isLoadingMore = false,
                                currentPage = latestState.currentPage + 1,
                                hasMorePages = newStores.size >= pageLimit
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                storesError = response.message ?: "Failed to load more results"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            storesError = e.message ?: "Failed to load more results"
                        )
                    }
                }
            }
        }
    }

    fun refreshData() {
        // Cancel any ongoing operations
        searchJob?.cancel()

        _uiState.update {
            it.copy(
                isRefreshing = true,
                searchQuery = "",
                stores = emptyList(),
                currentPage = 1,
                hasMorePages = true,
                storesError = ""
            )
        }

        // Reload categories and then stores
        viewModelScope.launch {
            loadCategories()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retryLoadStores() {
        val currentState = _uiState.value
        when {
            currentState.searchQuery.isNotEmpty() -> {
                searchStores(currentState.searchQuery)
            }
            currentState.selectedCategory != null -> {
                _uiState.update { it.copy(stores = emptyList(), isStoresLoading = true) }
                loadStoresByCategory(currentState.selectedCategory, isInitialLoad = true)
            }
        }
    }

    fun clearStoresError() {
        _uiState.update { it.copy(storesError = "") }
    }

    fun clearCategoriesError() {
        _uiState.update { it.copy(categoriesError = "") }
    }

    // Helper function to get fallback icon resource based on category name
    fun getFallbackIconRes(categoryName: String): Int {
        return when (categoryName.lowercase()) {
            "all" -> com.mobitechs.slashapp.R.drawable.all
            "food" -> com.mobitechs.slashapp.R.drawable.cat_food
            "grocery" -> com.mobitechs.slashapp.R.drawable.cat_grocery
            "fashion" -> com.mobitechs.slashapp.R.drawable.cat_fashion
            "health" -> com.mobitechs.slashapp.R.drawable.cat_health
            "wellness" -> com.mobitechs.slashapp.R.drawable.cat_health
            "beauty" -> com.mobitechs.slashapp.R.drawable.cat_food
            "electronics" -> com.mobitechs.slashapp.R.drawable.cat_fashion
            "others" -> com.mobitechs.slashapp.R.drawable.cat_food
            else -> com.mobitechs.slashapp.R.drawable.cat_food
        }
    }

    // Helper function to calculate distance
    fun calculateDistance(latitude: String?, longitude: String?): String {
        // TODO: Implement actual distance calculation using user's location
        return "${(1..5).random()}.${(0..9).random()} km away"
    }
}