package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.data.model.CategoryItem
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.ui.components.CategoryItemSize
import com.mobitechs.slashapp.ui.components.CommonCategoryItem
import com.mobitechs.slashapp.ui.components.CommonStoreCardHorizontal
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    // Fix double API call issue by using LaunchedEffect
    LaunchedEffect(Unit) {
        viewModel.initializeData()
    }

    // Observe UI state
    val uiState by viewModel.uiState.collectAsState()

    // State for Available Cashback vs Refer & Earn
    var selectedTab by remember { mutableStateOf(0) } // 0 = Available Cashback, 1 = Refer & Earn

    // Static data for daily rewards (keeping this static as it's not part of the API)
    val dailyRewards = remember {
        listOf(
            DailyReward("Spin and Win","Play and win daily rewards", R.drawable.spin_win, Color(0xFFE91E63),"Rewards"),
//            DailyReward("Survey","Take the survey and answer questions to win the rewards", R.drawable.survey, Color(
//                0xFF2196F3
//            ),"Survey"),
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF66BB6A),
                            Color(0xFFFCFCFC),
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp), // Space for bottom navigation
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Background image container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Adjust height as needed
                ) {
                    // Background image
                    Image(
                        painter = painterResource(id = R.drawable.home_bg),
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Content over background
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        // Top Bar with Card
                        TopAppBarCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // Cashback and Refer Section - Now Dynamic
                        CashbackSection(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            availableCashback = uiState.availableCashback,
                            totalEarned = uiState.totalEarned,
                            isLoading = uiState.isUserDataLoading,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Daily Rewards Section with full width
                DailyRewardsSection(
                    rewards = dailyRewards,
                    modifier = Modifier, // Remove start padding for full width
                    onDailyRewardCardClick = {
                        navController.navigate(Screen.DailyRewardsScreen.route)
                    },

                )
            }

            item {
                // Categories Section (below background) - Now Dynamic with Server Images
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
//                    CategoriesSection(
//                        categories = uiState.categories,
//                        isLoading = uiState.isCategoriesLoading,
//                        error = uiState.categoriesError,
//                        onRetry = { viewModel.loadCategories() },
//                        onClearError = { viewModel.clearCategoriesError() },
//                        viewModel = viewModel,
//                        modifier = Modifier
//                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CommonCategoryItem(
                                category = category,
                                isSelected = false, // No selection in home screen
                                onSelected = null,   // No selection handling in home screen
                                getFallbackIconRes = viewModel::getFallbackIconRes,
                                size = CategoryItemSize.Large
                            )
                        }
                    }
                }
            }

            item {
                // VIP Membership Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    VIPMembershipSectionNew(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                // Top Stores Section with horizontal scroll - Now Dynamic with Server Images
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    TopStoresSection(
                        stores = uiState.stores,
                        isLoading = uiState.isStoresLoading,
                        error = uiState.storesError,
                        onRetry = { viewModel.loadTopStores() },
                        onClearError = { viewModel.clearStoresError() },
                        viewModel = viewModel,
                        navController = navController,
                        modifier = Modifier.padding(start = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppBarCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF2D2D2D),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { }
            )

            // Location section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DLF Mall, Noida, U.P",
                    color = Color(0xFF2D2D2D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF2D2D2D),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF2D2D2D),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { }
                    )
                    // Notification badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .offset(x = 6.dp, y = (-2).dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CashbackSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    availableCashback: String,
    totalEarned: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle buttons
        Row(
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(20.dp)
                )
                .padding(4.dp)
                .border(
                    width = 1.5.dp,
                    color = SlashColors.Primary, shape = RoundedCornerShape(20.dp)
                )
        ) {
            ToggleButton(
                text = "Available Cashback",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            ToggleButton(
                text = "Refer & Earn",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount display based on selected tab - Now Dynamic
        if (isLoading) {
            CircularProgressIndicator(
                color = SlashColors.Primary,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Text(
                text = if (selectedTab == 0) availableCashback else totalEarned,
                color = SlashColors.Primary,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (selectedTab == 1) {
            Text(
                text = "Total Earned",
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(3.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SlashColors.Primary.copy(alpha = 0.8f) else Color.Transparent
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else SlashColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DailyRewardsSection(
    rewards: List<DailyReward>,
    modifier: Modifier = Modifier,
    onDailyRewardCardClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable {
                onDailyRewardCardClick()
            }
    ) {
        Text(
            text = "Daily Rewards",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlashColors.PrimaryText,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )

        // Full width daily rewards cards
        rewards.forEach { reward ->
            DailyRewardCard(
                reward = reward,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun DailyRewardCard(
    reward: DailyReward,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content area (80%) - Simplified approach
            Row(
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxHeight()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = reward.iconRes),
                    contentDescription = reward.title,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = reward.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = reward.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = SlashColors.SecondaryText
                    )
                }
            }

            // Dotted line divider
            Canvas(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            ) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    pathEffect = pathEffect,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Play Now area (20%)
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Play\nNow",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.Primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<CategoryItem>,
    isLoading: Boolean,
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = SlashColors.Primary,
                    strokeWidth = 2.dp
                )
            } else if (error.isNotEmpty()) {
                IconButton(
                    onClick = onRetry,
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
            error.isNotEmpty() -> {
                ErrorMessage(
                    message = error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            categories.isEmpty() && !isLoading -> {
                EmptyMessage(
                    message = "No categories available",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryItem,
    viewModel: HomeViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { }
            .width(100.dp)
    ) {
        Surface(
            modifier = Modifier.size(85.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = category.icon,
                    contentDescription = category.name,
                    modifier = Modifier.size(60.dp),
                    placeholder = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                    error = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                    fallback = painterResource(id = viewModel.getFallbackIconRes(category.name)),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.SecondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VIPMembershipSectionNew(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlashColors.VipCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // VIP icon/image
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vip),
                    contentDescription = "BecomeVIP",
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))

            // Vertical dotted line
            Canvas(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
            ) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                drawLine(
                    color = SlashColors.DottedLineColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    pathEffect = pathEffect,
                    strokeWidth = 2.dp.toPx()
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Content: Title, Description, Button (vertically arranged)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Become VIP Member Today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.PrimaryText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "and get special rewards & win more cashback",
                    fontSize = 12.sp,
                    color = SlashColors.SecondaryText,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlashColors.LearnMoreButton
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(width = 80.dp, height = 28.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = "Learn More",
                            fontSize = 10.sp,
                            color = SlashColors.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopStoresSection(
    stores: List<StoreListItem>,
    isLoading: Boolean,
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Stores Near you",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = SlashColors.Primary,
                    strokeWidth = 2.dp
                )
            } else if (error.isNotEmpty()) {
                IconButton(
                    onClick = onRetry,
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
            error.isNotEmpty() -> {
                ErrorMessage(
                    message = error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            stores.isEmpty() && !isLoading -> {
                EmptyMessage(
                    message = "No stores available",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(stores, key = { it.id }) { store ->
                        CommonStoreCardHorizontal(
                            store = store,
                            calculateDistance = viewModel::calculateDistance,
                            onStoreClick = {
                                navController.navigate("${Screen.StoreDetailsScreen.route}/${store.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreCard(
    store: StoreListItem,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlashColors.StoreCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store Name
            Text(
                text = store.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Store Image with server loading
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = SlashColors.StoreImageBackground
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = store.logo ?: store.banner_image,
                        contentDescription = store.name,
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.store_default),
                        error = painterResource(id = R.drawable.store_default),
                        fallback = painterResource(id = R.drawable.store_default),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Distance section with white background and no padding from start and end
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlashColors.StoreCardWhiteSection
            ) {
                Text(
                    text = "Distance - ${viewModel.calculateDistance(store.latitude, store.longitude)}",
                    fontSize = 12.sp,
                    color = SlashColors.DistanceText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Category, Minimum Order, VIP Discount section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Category - ${store.category_name}",
                    fontSize = 12.sp,
                    color = SlashColors.CategoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Minimum Order - ₹${store.minimum_order_amount}",
                    fontSize = 12.sp,
                    color = SlashColors.CategoryText
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIP Disc - ",
                        fontSize = 12.sp,
                        color = SlashColors.VipDiscountRed
                    )
                    Text(
                        text = "${store.vip_discount_percentage}%",
                        fontSize = 12.sp,
                        color = SlashColors.DiscountRed,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discount - ",
                        fontSize = 12.sp,
                        color = SlashColors.CategoryText
                    )
                    Text(
                        text = "${store.normal_discount_percentage}%",
                        fontSize = 12.sp,
                        color = SlashColors.CategoryText,
                        fontWeight = FontWeight.Medium
                    )
                }
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
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = SlashColors.SecondaryText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Data classes - keeping the existing ones for UI compatibility
data class Category(
    val name: String,
    val iconRes: Int
)

data class Store(
    val name: String,
    val category: String,
    val distance: String,
    val minimumOrder: String,
    val vipDiscount: String,
    val imageRes: Int
)

data class DailyReward(
    val title: String,
    val description: String,
    val iconRes: Int,
    val color: Color,
    val type: String,
)