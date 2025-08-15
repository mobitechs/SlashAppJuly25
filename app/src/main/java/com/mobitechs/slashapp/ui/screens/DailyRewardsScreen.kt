    package com.mobitechs.slashapp.ui.screens

    import SpinWheelContent
    import android.widget.Toast
    import androidx.activity.compose.BackHandler
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.*
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.mobitechs.slashapp.Screen
    import com.mobitechs.slashapp.data.model.SpinWheelCampaignItem
    import com.mobitechs.slashapp.data.model.SpinWheelSummeryData
    import com.mobitechs.slashapp.ui.viewmodels.DailyRewardsUiState
    import com.mobitechs.slashapp.ui.viewmodels.DailyRewardsViewModel

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DailyRewardsScreen(
        viewModel: DailyRewardsViewModel,
        navController: NavController,
        onBackClick: () -> Unit
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        BackHandler {
            if (uiState.showSpinWheel) {
                // If showing spin wheel, go back to dashboard
                viewModel.backFromSpinWheel()
            } else {
                // If showing dashboard, go back to previous screen (Home)
                onBackClick()
            }
        }

        // Toast observer
        val toastMessage by viewModel.toastMessage.collectAsState()
        LaunchedEffect(toastMessage) {
            toastMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
            ) {
                // Top Bar with dynamic title
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (uiState.showSpinWheel)
                                    uiState.currentCampaign?.title ?: "Spin & Win"
                                else
                                    "Daily Rewards",
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    if (uiState.showSpinWheel) {
                                        // If showing spin wheel, go back to dashboard
                                        viewModel.backFromSpinWheel()
                                    } else {
                                        // If showing dashboard, go back to previous screen
                                        onBackClick()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (uiState.showSpinWheel) {
                    // Show Spin Wheel Screen without header
                    SpinWheelContent(
                        viewModel = viewModel,
                        showHeader = false // Add this parameter to hide header
                    )
                } else {
                    // Show Dashboard with campaigns
                    DailyRewardsDashboard(
                        viewModel = viewModel,
                        uiState = uiState,
                        navController = navController
                    )
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    @Composable
    private fun DailyRewardsDashboard(
        viewModel: DailyRewardsViewModel,
        uiState: DailyRewardsUiState,
        navController: NavController
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Summary Card
            item {
                uiState.summaryData?.let { summary ->
                    SummaryCard(summary = summary)
                }
            }

            // Today's Status Header
            item {
                Text(
                    text = "Today's Opportunities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Campaigns List
            if (uiState.isCampaignsLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                items(uiState.campaigns) { campaign ->
                    CampaignCard(
                        campaign = campaign,
                        onClick = { viewModel.selectCampaign(campaign.id.toString()) }
                    )
                }
            }

            // History Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
//                        .clickable { viewModel.loadHistory() },
                        .clickable { navController.navigate(Screen.SpinWheelHistoryScreen.route) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "View Spin History",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SummaryCard(
        summary: SpinWheelSummeryData,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Your Spin Stats",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Total lifetime rewards",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Total Spins",
                        value = summary.total_statistics.total_spins.toString(),
                        icon = Icons.Default.Refresh
                    )
                    StatItem(
                        label = "Win Rate",
                        value = "${summary.total_statistics.win_rate}%",
                        icon = Icons.Default.TrendingUp
                    )
                    StatItem(
                        label = "Cashback Won",
                        value = "₹${summary.total_statistics.total_cashback_won}",
                        icon = Icons.Default.AccountBalanceWallet
                    )
                }
            }
        }
    }

    @Composable
    private fun StatItem(
        label: String,
        value: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun CampaignCard(
        campaign: SpinWheelCampaignItem,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = campaign.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = campaign.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Spin status
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (campaign.can_spin) Icons.Default.CheckCircle else Icons.Default.Block,
                            contentDescription = null,
                            tint = if (campaign.can_spin) Color.Green else Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (campaign.can_spin)
                                "${campaign.remaining_spins} spins left"
                            else
                                "Come back tomorrow",
                            fontSize = 12.sp,
                            color = if (campaign.can_spin) Color.Green else Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Spin wheel icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = if (campaign.can_spin)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TAP TO SPIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (campaign.can_spin)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }