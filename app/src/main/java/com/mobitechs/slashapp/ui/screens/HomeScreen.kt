package com.mobitechs.slashapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    // State for Available Cashback vs Refer & Earn
    var selectedTab by remember { mutableStateOf(0) } // 0 = Available Cashback, 1 = Refer & Earn

    // Static data - in real app, this would come from ViewModel
    val categories = remember {
        listOf(
            Category("Food", R.drawable.cat_food),
            Category("Grocery", R.drawable.cat_grocery),
            Category("Fashion", R.drawable.cat_fashion),
            Category("Health", R.drawable.cat_health),
            Category("Beauty", R.drawable.cat_food),
            Category("Electronics", R.drawable.cat_fashion)
        )
    }

    val dailyRewards = remember {
        listOf(
            DailyReward("Spin and Win","Play and win daily rewards", R.drawable.spin_win, Color(0xFFE91E63)),
//            DailyReward("Survey", "📝", Color(0xFF2196F3)),
//            DailyReward("Check In", "✅", Color(0xFF4CAF50))
        )
    }

    val stores = remember {
        listOf(
            Store(
                "ABC Store",
                "Grocery",
                "3 km away",
                "₹300",
                "7%",
                R.drawable.store_default
            ),
            Store(
                "XYZ Market",
                "Fashion",
                "2 km away",
                "₹500",
                "10%",
                R.drawable.store_default
            ),
            Store(
                "Health Plus",
                "Health",
                "1.5 km away",
                "₹200",
                "5%",
                R.drawable.store_default
            )
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
                        // Cashback and Refer Section
                        CashbackSection(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Daily Rewards Section with full width
                DailyRewardsSection(
                    rewards = dailyRewards,
                    modifier = Modifier // Remove start padding for full width
                )
            }

            item {
                // Categories Section (below background)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    CategoriesSection(
                        categories = categories,
                        modifier = Modifier // Keep as is for categories
                    )
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
                // Top Stores Section with horizontal scroll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    TopStoresSection(
                        stores = stores,
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

        // Amount display based on selected tab
        Text(
            text = if (selectedTab == 0) "₹3280" else "₹5550",
            color = SlashColors.Primary,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )

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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
            .height(100.dp)
            .clickable { },
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
    categories: List<Category>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlashColors.PrimaryText,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Reduced gap from 16dp to 8dp
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories, key = { it.name }) { category ->
                CategoryItem(category = category)
            }
        }
    }
}


@Composable
private fun CategoryItem(
    category: Category
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { }
            .width(100.dp) // Increased from 80dp to 100dp
    ) {
        Surface(
            modifier = Modifier.size(85.dp), // Increased from 60dp to 75dp
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = category.name,
                    modifier = Modifier.size(60.dp) // Increased from 32dp to 40dp
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
                    modifier = Modifier.size(60.dp) // Increased from 32dp to 40dp
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
    stores: List<Store>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Top Stores Near you",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlashColors.PrimaryText, // Changed from Color.White
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(stores, key = { it.name + it.category }) { store ->
                StoreCard(store = store)
            }
        }
    }
}

@Composable
private fun StoreCard(
    store: Store,
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
                    .padding(top = 16.dp, bottom = 12.dp)
            )

            // Store Image with no padding from start and end
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
                    Image(
                        painter = painterResource(id = store.imageRes),
                        contentDescription = store.name,
                        contentScale = ContentScale.Fit
                    )
                }
            }



            // Distance section with white background and no padding from start and end
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlashColors.StoreCardWhiteSection
            ) {
                Text(
                    text = "Distance - ${store.distance}",
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
                    text = "Category - ${store.category}",
                    fontSize = 12.sp,
                    color = SlashColors.CategoryText
                )
                Text(
                    text = "Minimum Order - ${store.minimumOrder}",
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
                        text = store.vipDiscount,
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
                        text = "7%",
                        fontSize = 12.sp,
                        color = SlashColors.CategoryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Data classes
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
    val color: Color
)

