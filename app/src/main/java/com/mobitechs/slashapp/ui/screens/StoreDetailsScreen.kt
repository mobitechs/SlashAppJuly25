package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.Screen
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for Rate Now button
        ) {
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
                                .height(400.dp),
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
                    // Store Image with Back Button Overlay
                    item {
                        Box {
                            AsyncImage(
                                model = uiState.store!!.banner_image ?: uiState.store!!.logo,
                                contentDescription = uiState.store!!.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.store_default),
                                error = painterResource(id = R.drawable.store_default)
                            )

                            // Back Button Overlay
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier
                                    .padding(top = 40.dp, start = 16.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Store Info Card
                    item {
                        StoreInfoCard(
                            store = uiState.store!!,
                            isFavorite = uiState.isFavorite,
                            isUpdatingFavorite = uiState.isUpdatingFavorite,
                            onToggleFavorite = { viewModel.toggleFavorite() },
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Reviews Section
                    if (uiState.reviews.isNotEmpty() || uiState.isReviewsLoading || uiState.reviewsError.isNotEmpty()) {
                        item {
                            ReviewsSection(
                                reviews = uiState.reviews,
                                isLoading = uiState.isReviewsLoading,
                                isLoadingMore = uiState.isLoadingMoreReviews,
                                error = uiState.reviewsError,
                                reviewCount = uiState.store!!.total_reviews,
                                helpfulReviews = uiState.helpfulReviews,
                                reportedReviews = uiState.reportedReviews,
                                markingInProgress = uiState.markingInProgress,
                                onRetry = { viewModel.retryLoadReviews() },
                                onMarkHelpful = { reviewId -> viewModel.markReviewHelpful(reviewId) },
                                onMarkReport = { reviewId -> viewModel.markReviewReport(reviewId) },
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 56.dp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Rate Now Button (Fixed at bottom)
        if (uiState.store != null) {
            Button(
                onClick = {
                    navController.navigate(Screen.AddReviewScreen.route + "/${storeId}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 56.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color like in design
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = if (uiState.store?.user_review != null) "Update Review" else "Rate Now", // Dynamic text based on user_review
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
    isFavorite: Boolean,
    isUpdatingFavorite: Boolean,
    onToggleFavorite: () -> Unit,
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
            // Store Name with Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.PrimaryText,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleFavorite,
                    enabled = !isUpdatingFavorite
                ) {
                    if (isUpdatingFavorite) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SlashColors.Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else SlashColors.SecondaryText
                        )
                    }
                }
            }

            // Rating (Number first, then stars)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = store.rating,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.PrimaryText
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) { index ->
                    val rating = store.rating.toFloatOrNull() ?: 0f
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address
            if (!store.address.isNullOrEmpty()) {
                Text(
                    text = store.address,
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText,
                    lineHeight = 18.sp
                )
            }

            // Distance and Get Direction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "13 Kms Away From You",
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText
                )

                Text(
                    text = "Get Direction",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50), // Green color
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { /* Handle get direction */ }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number with Call Intent
            if (!store.phone_number.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Handle phone call */ }
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

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            if (!store.description.isNullOrEmpty()) {
                Text(
                    text = store.description!!,
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category and VIP Discount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category - ${store.category_name}",
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText
                )

                if (store.vip_discount_percentage.toDoubleOrNull()?.let { it > 0 } == true) {
                    Text(
                        text = "VIP Disc - ${formatPercentage(store.vip_discount_percentage)}%",
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Minimum Order and Discount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Minimum Order : ${formatAmount(store.minimum_order_amount)}",
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText
                )

                Text(
                    text = "Discount - ${formatPercentage(store.normal_discount_percentage)}%",
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText
                )
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<StoreReviewsListItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String,
    reviewCount: String,
    helpfulReviews: Map<Int, Boolean>,
    reportedReviews: Map<Int, Boolean>,
    markingInProgress: Set<Int>,
    onRetry: () -> Unit,
    onMarkHelpful: (Int) -> Unit,
    onMarkReport: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Reviews Header
        Text(
            text = "Reviews: $reviewCount",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlashColors.PrimaryText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            error.isNotEmpty() -> {
                ErrorMessage(
                    message = error,
                    onRetry = onRetry
                )
            }

            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SlashColors.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            reviews.isEmpty() -> {
                EmptyMessage(message = "No reviews available")
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        reviews.forEachIndexed { index, review ->
                            ReviewItem(
                                review = review,
                                isHelpful = helpfulReviews[review.id] ?: false,
                                isReported = reportedReviews[review.id] ?: false,
                                isMarkingInProgress = markingInProgress.contains(review.id),
                                onMarkHelpful = { onMarkHelpful(review.id) },
                                onMarkReport = { onMarkReport(review.id) }
                            )

                            if (index < reviews.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp
                                )
                            }
                        }

                        // Loading more indicator
                        if (isLoadingMore) {
                            Divider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = Color(0xFFE0E0E0),
                                thickness = 1.dp
                            )
                            Box(
                                modifier = Modifier.fillMaxWidth(),
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

// Update ReviewItem function
@Composable
private fun ReviewItem(
    review: StoreReviewsListItem,
    isHelpful: Boolean,
    isReported: Boolean,
    isMarkingInProgress: Boolean,
    onMarkHelpful: () -> Unit,
    onMarkReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // User info and rating
        Row(
            modifier = Modifier.fillMaxWidth(),
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

            // Name
            Text(
                text = "${review.first_name} ${review.last_name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.PrimaryText,
                modifier = Modifier.weight(1f)
            )

            // Rating stars
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < review.rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Title
        if (review.title.isNotEmpty()) {
            Text(
                text = review.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Description
        if (review.description.isNotEmpty()) {
            Text(
                text = review.description,
                fontSize = 14.sp,
                color = SlashColors.SecondaryText,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 18.sp
            )
        }

        // Like/Dislike and Date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                // Helpful (Like) Button
                if (isMarkingInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SlashColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Mark as helpful",
                        tint = if (isHelpful) Color.Blue else SlashColors.SecondaryText, // Blue if marked helpful
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onMarkHelpful() }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Report (Dislike) Button
                if (!isMarkingInProgress) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Report review",
                        tint = if (isReported) Color.Red else SlashColors.SecondaryText, // Red if marked as report
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onMarkReport() }
                    )
                }
            }

            Text(
                text = review.created_at.take(10), // Show only date part
                fontSize = 12.sp,
                color = SlashColors.SecondaryText
            )
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