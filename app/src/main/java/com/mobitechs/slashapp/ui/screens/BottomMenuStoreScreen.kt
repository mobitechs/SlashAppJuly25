package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.data.model.CategoryItem
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuStoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuStoreScreen(
    viewModel: BottomMenuStoreViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Check if user has scrolled to the end for pagination
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItemIndex = (visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                lastVisibleItemIndex > (layoutInfo.totalItemsCount - 3)
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.hasMorePages && !uiState.isLoadingMore) {
            if (uiState.searchQuery.isNotEmpty()) {
                viewModel.loadMoreSearchResults()
            } else {
                viewModel.loadMoreStores()
            }
        }
    }

    // Background color for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Light gray background
            .statusBarsPadding()
    ) {
        // Single LazyColumn for entire content
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Top App Bar (scrollable)
            item(key = "top_app_bar") {
                TopAppBar(
                    title = {
                        Text(
                            text = "All Stores",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlashColors.PrimaryText
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = SlashColors.PrimaryText
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF8F9FA) // Same background as screen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Location Bar (scrollable)
            item(key = "location_bar") {
                LocationBar(
                    location = "DLF Mall, Noida, U.P",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Categories Section (sticky header)
            if (uiState.categories.isNotEmpty()) {
                stickyHeader(key = "categories") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FA)) // Same background as screen
                            .padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Categories",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlashColors.PrimaryText
                            )

                            if (uiState.isCategoriesLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = SlashColors.Primary,
                                    strokeWidth = 2.dp
                                )
                            } else if (uiState.categoriesError.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.refreshData() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        tint = SlashColors.Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        when {
                            uiState.categoriesError.isNotEmpty() -> {
                                ErrorMessage(
                                    message = uiState.categoriesError,
                                    onRetry = { viewModel.refreshData() },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            uiState.categories.isEmpty() && !uiState.isCategoriesLoading -> {
                                EmptyMessage(
                                    message = "No categories available",
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            else -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    items(uiState.categories, key = { it.id }) { category ->
                                        CategoryItemCard(
                                            category = category,
                                            isSelected = uiState.selectedCategory?.id == category.id,
                                            onSelected = { viewModel.selectCategory(category) },
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar (sticky header)
            stickyHeader(key = "search_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA)) // Same background as screen
                        .padding(bottom = 8.dp)
                ) {
                    SearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                        isSearching = uiState.isSearching,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // Store List Content
            when {
                uiState.storesError.isNotEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            ErrorMessage(
                                message = uiState.storesError,
                                onRetry = { viewModel.retryLoadStores() }
                            )
                        }
                    }
                }
                uiState.isStoresLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = SlashColors.Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                uiState.stores.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyMessage(
                                message = if (uiState.searchQuery.isNotEmpty()) {
                                    "No stores found for \"${uiState.searchQuery}\""
                                } else {
                                    "No stores available"
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Store Items
//                    items(uiState.stores, key = { it.id }) { store ->
//                        StoreListCard(
//                            store = store,
//                            viewModel = viewModel,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 16.dp, vertical = 6.dp)
//                        )
//                    }

                    items(
                        items = uiState.stores,
                        key = { store -> "store_${store.id}" }
                    ) { store ->
                        StoreListCard(
                            store = store,
                            viewModel = viewModel,
                            onStoreClick = { storeId ->
                                navController.navigate("${Screen.StoreDetailsScreen.route}/${storeId}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SlashColors.Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: CategoryItem,
    isSelected: Boolean,
    onSelected: () -> Unit,
    viewModel: BottomMenuStoreViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSelected() }
            .width(70.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, SlashColors.Primary, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            shape = CircleShape,
            color = if (isSelected) SlashColors.Primary.copy(alpha = 0.1f) else Color.White,
            shadowElevation = if (isSelected) 4.dp else 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (category.name == "All") {
                    Icon(
                        painter = painterResource(id = R.drawable.cat_food),
                        contentDescription = category.name,
                        modifier = Modifier.size(30.dp),
                        tint = if (isSelected) SlashColors.Primary else SlashColors.SecondaryText
                    )
                } else {
                    AsyncImage(
                        model = category.icon,
                        contentDescription = category.name,
                        modifier = Modifier.size(30.dp),
                        placeholder = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                        error = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                        fallback = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) SlashColors.Primary else SlashColors.SecondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocationBar(
    location: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.background(Color(0xFFF8F9FA)), // Same background
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = SlashColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = location,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.PrimaryText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search any store or category...",
                color = SlashColors.SecondaryText,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SlashColors.Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SlashColors.SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChanged("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = SlashColors.SecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SlashColors.Primary,
            unfocusedBorderColor = SlashColors.SecondaryText.copy(alpha = 0.3f),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        singleLine = true
    )
}


@Composable
private fun StoreListCard(
    store: StoreListItem,
    viewModel: BottomMenuStoreViewModel,
    onStoreClick: (String) -> Unit, // Add this parameter
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onStoreClick(store.id.toString()) } // Pass store ID to callback
            .border(1.dp, SlashColors.VipBackground, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // ... rest of your existing card content remains the same
        Column {
            // Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Store Image
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SlashColors.StoreImageBackground
                ) {
                    AsyncImage(
                        model = store.logo ?: store.banner_image,
                        contentDescription = store.name,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.store_default),
                        error = painterResource(id = R.drawable.store_default),
                        fallback = painterResource(id = R.drawable.store_default),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Store Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Store Name
                    Text(
                        text = store.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlashColors.PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Distance - reduced spacing
                    Text(
                        text = "13 Km away from you",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Category - reduced spacing
                    Text(
                        text = "Category - ${store.category_name}",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Minimum Order and Discount in same row - reduced spacing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Minimum Order - format to remove .00
                        Text(
                            text = "Min Order - ₹${formatAmount(store.minimum_order_amount)}",
                            fontSize = 12.sp,
                            color = SlashColors.SecondaryText,
                            modifier = Modifier.weight(1f)
                        )

                        // Discount - format to remove .00
                        Text(
                            text = "Discount - ${formatPercentage(store.normal_discount_percentage)}%",
                            fontSize = 12.sp,
                            color = SlashColors.SecondaryText,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // VIP Discount Badge - Full width at bottom
            if (store.vip_discount_percentage.toDoubleOrNull()?.let { it > 0 } == true) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SlashColors.VipBackground,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start icon
                        Image(
                            painter = painterResource(id = R.drawable.vip_star),
                            contentDescription = "VIP",
                            modifier = Modifier.size(28.dp),
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Center text
                            Text(
                                text = "Become VIP & Get",
                                fontSize = 12.sp,
                                color = SlashColors.White,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "VIP Discount of ${formatPercentage(store.vip_discount_percentage)}%",
                                fontSize = 12.sp,
                                color = SlashColors.WarningOrange,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.offset(y = (-2).dp)
                            )
                        }
                        // End icon
                        Image(
                            painter = painterResource(id = R.drawable.vip_star),
                            contentDescription = "VIP",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    }
}


//@Composable
//private fun StoreListCard(
//    store: StoreListItem,
//    viewModel: BottomMenuStoreViewModel,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        modifier = modifier
//            .clickable { /* Navigate to store details */ }
//            .border(1.dp, SlashColors.VipBackground, RoundedCornerShape(12.dp)),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column {
//            // Main content row
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(12.dp)
//            ) {
//                // Store Image
//                Surface(
//                    modifier = Modifier.size(80.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    color = SlashColors.StoreImageBackground
//                ) {
//                    AsyncImage(
//                        model = store.logo ?: store.banner_image,
//                        contentDescription = store.name,
//                        contentScale = ContentScale.Crop,
//                        placeholder = painterResource(id = R.drawable.store_default),
//                        error = painterResource(id = R.drawable.store_default),
//                        fallback = painterResource(id = R.drawable.store_default),
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                // Store Details
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    // Store Name
//                    Text(
//                        text = store.name,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = SlashColors.PrimaryText,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//
//                    // Distance - reduced spacing
//                    Text(
//                        text = "13 Km away from you",
//                        fontSize = 12.sp,
//                        color = SlashColors.SecondaryText,
//                        modifier = Modifier.padding(top = 2.dp)
//                    )
//
//                    // Category - reduced spacing
//                    Text(
//                        text = "Category - ${store.category_name}",
//                        fontSize = 12.sp,
//                        color = SlashColors.SecondaryText,
//                        modifier = Modifier.padding(top = 2.dp)
//                    )
//
//                    // Minimum Order and Discount in same row - reduced spacing
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 2.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        // Minimum Order - format to remove .00
//                        Text(
//                            text = "Min Order - ₹${formatAmount(store.minimum_order_amount)}",
//                            fontSize = 12.sp,
//                            color = SlashColors.SecondaryText,
//                            modifier = Modifier.weight(1f)
//                        )
//
//                        // Discount - format to remove .00
//                        Text(
//                            text = "Discount - ${formatPercentage(store.normal_discount_percentage)}%",
//                            fontSize = 12.sp,
//                            color = SlashColors.SecondaryText,
//                            textAlign = TextAlign.End
//                        )
//                    }
//                }
//            }
//
//            // VIP Discount Badge - Full width at bottom
//            if (store.vip_discount_percentage.toDoubleOrNull()?.let { it > 0 } == true) {
//                Surface(
//                    modifier = Modifier.fillMaxWidth(),
//                    color = SlashColors.VipBackground,
//                    shape = RoundedCornerShape(
//                        topStart = 0.dp,
//                        topEnd = 0.dp,
//                        bottomStart = 12.dp,
//                        bottomEnd = 12.dp
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 18.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // Start icon
//                        Image(
//                            painter = painterResource(id = R.drawable.vip_star),
//                            contentDescription = "VIP",
//                            modifier = Modifier.size(28.dp),
//                        )
//
//                        Column(
//                            modifier = Modifier.weight(1f),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            // Center text
//                            Text(
//                                text = "Become VIP & Get",
//                                fontSize = 12.sp,
//                                color = SlashColors.White,
//                                fontWeight = FontWeight.Medium,
//                                textAlign = TextAlign.Center,
//                            )
//                            Text(
//                                text = "VIP Discount of ${formatPercentage(store.vip_discount_percentage)}%",
//                                fontSize = 12.sp,
//                                color = SlashColors.WarningOrange,
//                                fontWeight = FontWeight.Medium,
//                                textAlign = TextAlign.Center,
//                                modifier = Modifier.offset(y = (-2).dp)
//                            )
//                        }
//                        // End icon
//                        Image(
//                            painter = painterResource(id = R.drawable.vip_star),
//                            contentDescription = "VIP",
//                            modifier = Modifier.size(28.dp),
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Retry",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.store_default),
                contentDescription = "No stores",
                modifier = Modifier.size(80.dp),
                tint = SlashColors.SecondaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = SlashColors.SecondaryText,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper functions to format amounts and percentages
private fun formatAmount(amount: String): String {
    return try {
        val doubleValue = amount.toDouble()
        if (doubleValue == doubleValue.toInt().toDouble()) {
            doubleValue.toInt().toString()
        } else {
            amount
        }
    } catch (e: NumberFormatException) {
        amount
    }
}

private fun formatPercentage(percentage: String): String {
    return try {
        val doubleValue = percentage.toDouble()
        if (doubleValue == doubleValue.toInt().toDouble()) {
            doubleValue.toInt().toString()
        } else {
            percentage
        }
    } catch (e: NumberFormatException) {
        percentage
    }
}