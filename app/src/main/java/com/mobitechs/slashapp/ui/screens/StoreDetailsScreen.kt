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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.model.StoreReviewsListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.StoreDetailsViewModel
import com.mobitechs.slashapp.utils.formatAmount
import com.mobitechs.slashapp.utils.formatPercentage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailsScreen(
    viewModel: StoreDetailsViewModel,
    navController: NavController,
    storeId: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Load data when screen is first opened
    LaunchedEffect(storeId) {
        viewModel.loadStoreDetails(storeId)
    }

    // Check if user has scrolled to the end for pagination (reviews)
    val shouldLoadMoreReviews by remember {
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

    LaunchedEffect(shouldLoadMoreReviews) {
        if (shouldLoadMoreReviews && uiState.hasMoreReviews && !uiState.isLoadingMoreReviews) {
            viewModel.loadMoreReviews()
        }
    }

    // Background color for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for Rate Now button
        ) {
            // Top App Bar
            item {
                TopAppBar(
                    title = { Text("") }, // Empty title to show only icons
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = SlashColors.PrimaryText
                            )
                        }
                    },
                    actions = {
                        // Refresh Button
                        IconButton(onClick = { viewModel.refreshData() }) {
                            if (uiState.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SlashColors.Primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = SlashColors.Primary
                                )
                            }
                        }

                        // Favorite Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite() },
                            enabled = !uiState.isUpdatingFavorite
                        ) {
                            if (uiState.isUpdatingFavorite) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SlashColors.Primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (uiState.isFavorite) Color.Red else SlashColors.SecondaryText
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF8F9FA)
                    )
                )
            }

            // Store Content
            when {
                uiState.storeError.isNotEmpty() -> {
                    item {
                        ErrorMessage(
                            message = uiState.storeError,
                            onRetry = { viewModel.retryLoadStore() },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                uiState.isStoreLoading -> {
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
                uiState.store != null -> {
                    // Store Image
                    item {
                        AsyncImage(
                            model = uiState.store!!.banner_image ?: uiState.store!!.logo,
                            contentDescription = uiState.store!!.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.store_default),
                            error = painterResource(id = R.drawable.store_default)
                        )
                    }

                    // Store Info Card
                    item {
                        StoreInfoCard(
                            store = uiState.store!!,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Get Direction Button
                    item {
                        Button(
                            onClick = { /* Handle get direction */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SlashColors.Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Get Direction",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Store Description
                    if (!uiState.store!!.description.isNullOrEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "About Store",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlashColors.PrimaryText
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.store!!.description!!,
                                        fontSize = 14.sp,
                                        color = SlashColors.SecondaryText,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // Store Details Card
                    item {
                        StoreDetailsCard(
                            store = uiState.store!!,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // VIP Discount Card
                    if (uiState.store!!.vip_discount_percentage.toDoubleOrNull()?.let { it > 0 } == true) {
                        item {
                            VipDiscountCard(
                                vipDiscount = uiState.store!!.vip_discount_percentage,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Reviews Section Header
                    item {
                        ReviewsHeader(
                            reviewCount = uiState.store!!.total_reviews,
                            onViewAll = { /* Handle view all reviews */ },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Reviews List
                    when {
                        uiState.reviewsError.isNotEmpty() -> {
                            item {
                                ErrorMessage(
                                    message = uiState.reviewsError,
                                    onRetry = { viewModel.retryLoadReviews() },
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        uiState.isReviewsLoading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = SlashColors.Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        uiState.reviews.isEmpty() -> {
                            item {
                                EmptyMessage(
                                    message = "No reviews available",
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }
                        else -> {
                            items(uiState.reviews, key = { it.id }) { review ->
                                ReviewCard(
                                    review = review,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }

                            // Loading more indicator
                            if (uiState.isLoadingMoreReviews) {
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

        // Rate Now Button (Fixed at bottom)
        if (uiState.store != null) {
            Button(
                onClick = { viewModel.showAddReviewDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlashColors.Primary
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Rate Now",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StoreInfoCard(
    store: StoreListItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Store Name
            Text(
                text = store.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SlashColors.PrimaryText
            )

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                repeat(5) { index ->
                    val rating = store.rating.toFloatOrNull() ?: 0f
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = store.rating,
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText
                )
            }

            // Address
            if (!store.address.isNullOrEmpty()) {
                Text(
                    text = store.address,
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 18.sp
                )
            }

            // Phone Number
            if (!store.phone_number.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { /* Handle phone call */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = SlashColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = store.phone_number,
                        fontSize = 14.sp,
                        color = SlashColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreDetailsCard(
    store: StoreListItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText
                    )
                    Text(
                        text = store.category_name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.PrimaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Minimum Order",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText
                    )
                    Text(
                        text = "₹${formatAmount(store.minimum_order_amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.PrimaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discount",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText
                    )
                    Text(
                        text = "${formatPercentage(store.normal_discount_percentage)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.PrimaryText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VipDiscountCard(
    vipDiscount: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlashColors.VipBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.vip_star),
                contentDescription = "VIP",
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VIP Discount",
                    fontSize = 14.sp,
                    color = SlashColors.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatPercentage(vipDiscount)}%",
                    fontSize = 16.sp,
                    color = SlashColors.WarningOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.vip_star),
                contentDescription = "VIP",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ReviewsHeader(
    reviewCount: String,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Reviews: $reviewCount",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlashColors.PrimaryText
        )

        Text(
            text = "View All",
            fontSize = 14.sp,
            color = SlashColors.Primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onViewAll() }
        )
    }
}

@Composable
private fun ReviewCard(
    review: StoreReviewsListItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = SlashColors.Primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = review.first_name.firstOrNull()?.toString() ?: "U",
                            color = SlashColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${review.first_name} ${review.last_name}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.PrimaryText
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < review.rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Text(
                    text = review.created_at.take(10), // Show only date part
                    fontSize = 12.sp,
                    color = SlashColors.SecondaryText
                )
            }

            if (review.title.isNotEmpty()) {
                Text(
                    text = review.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.PrimaryText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (review.description.isNotEmpty()) {
                Text(
                    text = review.description,
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText,
                    modifier = Modifier.padding(top = 4.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

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
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.store_default),
                contentDescription = "Empty",
                modifier = Modifier.size(60.dp),
                tint = SlashColors.SecondaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = SlashColors.SecondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}