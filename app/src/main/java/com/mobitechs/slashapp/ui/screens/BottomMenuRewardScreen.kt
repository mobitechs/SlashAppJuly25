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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.data.model.AvailableCoupon
import com.mobitechs.slashapp.data.model.RewardHistory
import com.mobitechs.slashapp.data.model.RewardSummeryData
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuRewardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuRewardScreen(
    viewModel: BottomMenuRewardViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Check if user has scrolled to the end for pagination (only for history)
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0 || uiState.selectedTab != 1) {
                false
            } else {
                val lastVisibleItemIndex = (visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
                lastVisibleItemIndex > (layoutInfo.totalItemsCount - 3)
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.hasMoreHistoryPages && !uiState.isLoadingMoreHistory && uiState.selectedTab == 1) {
            viewModel.loadMoreRewardHistory()
        }
    }

    // Background color for entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlashColors.Primary)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Rewards",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlashColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Content with white background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Tab Selection Section
                    item(key = "tab_section") {
                        TabSelectionSection(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { viewModel.setSelectedTab(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }

                    // Summary Section
                    item(key = "summary_section") {
                        SummarySection(
                            summaryData = uiState.rewardSummary,
                            isLoading = uiState.isSummaryLoading,
                            error = uiState.summaryError,
                            onRetry = { viewModel.retryLoadSummary() },
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Content Section based on selected tab
                    when (uiState.selectedTab) {
                        0 -> {
                            // Rewards Tab - Show Available Coupons
                            when {
                                uiState.couponsError.isNotEmpty() -> {
                                    item(key = "coupons_error") {
                                        ErrorMessage(
                                            message = uiState.couponsError,
                                            onRetry = { viewModel.retryLoadCoupons() },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                                uiState.isCouponsLoading -> {
                                    item(key = "coupons_loading") {
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
                                uiState.availableCoupons.isEmpty() -> {
                                    item(key = "coupons_empty") {
                                        EmptyMessage(
                                            message = "No available coupons",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                                        )
                                    }
                                }
                                else -> {
                                    item(key = "coupons_header") {
                                        Text(
                                            text = "Available Coupons",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlashColors.PrimaryText,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }

                                    itemsIndexed(
                                        items = uiState.availableCoupons,
                                        key = { index, coupon -> "coupon_${coupon.coupon_id}_$index" }
                                    ) { index, coupon ->
                                        CouponItem(
                                            coupon = coupon,
                                            viewModel = viewModel,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // History Tab - Show Reward History
                            when {
                                uiState.historyError.isNotEmpty() -> {
                                    item(key = "history_error") {
                                        ErrorMessage(
                                            message = uiState.historyError,
                                            onRetry = { viewModel.retryLoadHistory() },
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                                uiState.isHistoryLoading -> {
                                    item(key = "history_loading") {
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
                                uiState.rewardHistory.isEmpty() -> {
                                    item(key = "history_empty") {
                                        EmptyMessage(
                                            message = "No reward history available",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                                        )
                                    }
                                }
                                else -> {
                                    item(key = "history_header") {
                                        Text(
                                            text = "Reward History",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SlashColors.PrimaryText,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }

                                    itemsIndexed(
                                        items = uiState.rewardHistory,
                                        key = { index, history -> "history_${history.id}_$index" }
                                    ) { index, history ->
                                        RewardHistoryItem(
                                            rewardHistory = history,
                                            viewModel = viewModel,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }

                                    // Loading more indicator
                                    if (uiState.isLoadingMoreHistory) {
                                        item(key = "loading_more_history") {
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
            }
        }
    }
}

@Composable
private fun TabSelectionSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .background(
                    Color(0xFFF5F5F5),
                    RoundedCornerShape(20.dp)
                )
                .padding(4.dp)
        ) {
            TabButton(
                text = "Rewards",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            TabButton(
                text = "History",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SlashColors.Primary else Color.Transparent
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else SlashColors.SecondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

// Helper functions for reward history display
private fun getHistoryTitle(rewardHistory: RewardHistory): String {
    return when {
        !rewardHistory.coupon_name.isNullOrEmpty() -> "Coupon Title"
        rewardHistory.type?.lowercase() == "credit" -> "Cashback Earned"
        rewardHistory.type?.lowercase() == "debit" -> "Cashback Redeemed"
        else -> rewardHistory.reward_for ?: "Reward Activity"
    }
}

private fun getHistoryDescription(rewardHistory: RewardHistory): String {
    return when {
        rewardHistory.is_transaction -> {
            "Transaction at ${rewardHistory.store_name ?: "store"} with ${if (rewardHistory.type?.lowercase() == "credit") "cashback earned" else "payment processed"}"
        }
        !rewardHistory.coupon_name.isNullOrEmpty() -> {
            "Coupon used: ${rewardHistory.coupon_name} at ${rewardHistory.store_name ?: "store"}"
        }
        else -> {
            rewardHistory.reward_for ?: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod"
        }
    }
}

@Composable
private fun SummarySection(
    summaryData: RewardSummeryData?,
    isLoading: Boolean,
    error: String,
    onRetry: () -> Unit,
    viewModel: BottomMenuRewardViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SlashColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            summaryData != null -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Available Cashback
                    SummaryCard(
                        title = "Available Cashback",
                        amount = viewModel.formatAmount(summaryData.wallet.available_cashback.toInt()),
                        modifier = Modifier.weight(1f)
                    )

                    // Cashback Redeemed
                    SummaryCard(
                        title = "Cashback Redeemed",
                        amount = viewModel.formatAmount(summaryData.recent_activity.debits_last_30_days),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Coupons Redeemed
                    SummaryCard(
                        title = "Coupons Redeemed",
                        amount = "${summaryData.coupons.used_count}",
                        modifier = Modifier.weight(1f)
                    )

                    // Discount Amount (using credits as discount amount)
                    SummaryCard(
                        title = "Discount Amount",
                        amount = viewModel.formatAmount(summaryData.recent_activity.credits_last_30_days),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = SlashColors.SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlashColors.PrimaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CouponItem(
    coupon: AvailableCoupon,
    viewModel: BottomMenuRewardViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = coupon.coupon_name ?: "Unknown Coupon",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlashColors.PrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = coupon.description ?: "No description available",
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlashColors.Primary.copy(alpha = 0.1f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = coupon.discount_display ?: "0%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlashColors.Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Store: ${coupon.applicable_store_name ?: "All Stores"}",
                    fontSize = 12.sp,
                    color = SlashColors.SecondaryText
                )

                when {
                    coupon.expiry_date != null && coupon.expiry_date.toString().isNotEmpty() && coupon.expiry_date.toString() != "null" -> {
                        Text(
                            text = "Expires: ${viewModel.formatDate(coupon.expiry_date.toString())}",
                            fontSize = 10.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    coupon.lifetime_validity == 1 -> {
                        Text(
                            text = "Lifetime Valid",
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        Text(
                            text = "No Expiry",
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//private fun RewardHistoryItem(
//    rewardHistory: RewardHistory,
//    viewModel: BottomMenuRewardViewModel,
//    modifier: Modifier = Modifier
//) {
//    Box(
//        modifier = modifier.fillMaxWidth()
//    ) {
//        // First layer - Background image with date badge
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(140.dp)
//        ) {
//            // Background image (back layer)
//            Image(
//                painter = painterResource(id = R.drawable.reward_history_back_bg),
//                contentDescription = null,
//                modifier = Modifier
//                    .width(130.dp) // Set specific width
//                    .height(140.dp)
//                    .padding(top = 5.dp, end = 3.dp)
//                    .align(Alignment.CenterEnd), // Align to end
//                contentScale = ContentScale.FillBounds
//            )
//
//            // Date badge on top left of background image
//            if (!rewardHistory.date.isNullOrBlank()) {
//                Text(
//                    text = viewModel.formatDate(rewardHistory.date),
//                    color = Color.White,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp).align(Alignment.TopEnd),
//                )
//            }
//        }
//
//        // Second layer - Main content image (overlay)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//
//        ) {
//            // Main content background image (front layer)
//            Image(
//                painter = painterResource(id = R.drawable.reward_history_bg),
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(140.dp),
//                contentScale = ContentScale.FillBounds
//            )
//
//            // Content overlay on the main image
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(140.dp)
//                    .padding(16.dp)
//            ) {
//                // Main content
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(end = 20.dp) // Small padding to avoid edge
//                ) {
//                    // Title based on reward type
//                    Text(
//                        text = getRewardTitle(rewardHistory),
//                        color = Color.White,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Description
//                    Text(
//                        text = getRewardDescription(rewardHistory),
//                        color = Color.White.copy(alpha = 0.9f),
//                        fontSize = 14.sp,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis,
//                        lineHeight = 18.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Reward Amount - only show if amount > 0
//                    if (rewardHistory.amount > 0) {
//                        Text(
//                            text = "Reward Amount: ${viewModel.formatAmount(rewardHistory.amount)}",
//                            color = Color.White,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//
//                        Spacer(modifier = Modifier.height(4.dp))
//                    }
//
//                    // Store Name - only show if available
//                    if (!rewardHistory.store_name.isNullOrBlank()) {
//                        Text(
//                            text = "Store Name: ${rewardHistory.store_name}",
//                            color = Color.White.copy(alpha = 0.9f),
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//
//                // Time - bottom right
//                if (!rewardHistory.date.isNullOrBlank()) {
//                    Row(
//                        modifier = Modifier.align(Alignment.BottomEnd),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "🕐",
//                            color = Color.White.copy(alpha = 0.7f),
//                            fontSize = 12.sp
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = viewModel.formatTime(rewardHistory.date),
//                            color = Color.White.copy(alpha = 0.7f),
//                            fontSize = 12.sp
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
private fun RewardHistoryItem(
    rewardHistory: RewardHistory,
    viewModel: BottomMenuRewardViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // First layer - Background image with date badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background image (back layer)
            Image(
                painter = painterResource(id = R.drawable.reward_history_back_bg),
                contentDescription = null,
                modifier = Modifier
                    .width(130.dp) // Set specific width
                    .height(140.dp)
                    .padding(top = 5.dp, end = 3.dp)
                    .align(Alignment.CenterEnd), // Align to end
                contentScale = ContentScale.FillBounds
            )

            // Date badge on top left of background image
            if (!rewardHistory.date.isNullOrBlank()) {
                Text(
                    text = viewModel.formatDate(rewardHistory.date),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .align(Alignment.TopEnd),
                )
            }
        }

        // Second layer - Main content image (overlay)
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main content background image (front layer)
            Image(
                painter = painterResource(id = R.drawable.reward_history_bg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.FillBounds
            )

            // Content overlay on the main image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(16.dp)
            ) {
                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp) // Small padding to avoid edge
                ) {
                    // Title based on reward type
                    Text(
                        text = getRewardTitle(rewardHistory),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = getRewardDescription(rewardHistory),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reward Amount - only show if amount > 0
                    if (rewardHistory.amount > 0) {
                        Text(
                            text = "Reward Amount: ${viewModel.formatAmount(rewardHistory.amount)}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Coupon Code - only show if it's a coupon reward and code is available
                    if (!rewardHistory.coupon_name.isNullOrBlank()) {
                        Text(
                            text = "Coupon Code: ${rewardHistory.coupon_name}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Store Name - only show if available
                    if (!rewardHistory.store_name.isNullOrBlank()) {
                        Text(
                            text = "Store Name: ${rewardHistory.store_name}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }

                // Time - bottom right
                if (!rewardHistory.date.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🕐",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.formatTime(rewardHistory.date),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
// Updated helper function for reward titles
private fun getRewardTitle(rewardHistory: RewardHistory): String {
    return when {
        !rewardHistory.coupon_name.isNullOrBlank() -> "Coupon Reward"
        rewardHistory.reward_for?.contains("referral", ignoreCase = true) == true -> "Referral Reward"
        rewardHistory.reward_for?.contains("refer", ignoreCase = true) == true -> "Referral Reward"
        rewardHistory.type?.lowercase() == "credit" -> "Cashback Earned"
        rewardHistory.type?.lowercase() == "debit" -> "Cashback Redeemed"
        else -> rewardHistory.reward_for ?: "Reward Activity"
    }
}

// Updated helper function for descriptions
private fun getRewardDescription(rewardHistory: RewardHistory): String {
    return when {
        rewardHistory.is_transaction -> {
            buildString {
                append("Transaction completed successfully")
                if (!rewardHistory.store_name.isNullOrBlank()) {
                    append(" at ${rewardHistory.store_name}")
                }
                append(" with ${if (rewardHistory.type?.lowercase() == "credit") "cashback earned" else "payment processed"}")
            }
        }
        !rewardHistory.coupon_name.isNullOrBlank() -> {
            "Coupon '${rewardHistory.coupon_name}' applied successfully for savings"
        }
        !rewardHistory.reward_for.isNullOrBlank() -> {
            rewardHistory.reward_for
        }
        else -> {
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod"
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
                painter = painterResource(id = R.drawable.cat_food),
                contentDescription = "Empty",
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