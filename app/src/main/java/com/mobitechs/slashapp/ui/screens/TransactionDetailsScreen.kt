package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.data.model.MyTransactionListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.TransactionDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionDetailsViewModel,
    navController: NavController,
    transactionId: String
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.loadTransactionDetails(transactionId)
    }

    // Background color for entire screen with primary color
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
                        text = "Transaction Details",
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
                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = SlashColors.Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    uiState.error.isNotEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ErrorMessage(
                                message = uiState.error,
                                onRetry = { viewModel.retryLoadTransaction(transactionId) }
                            )
                        }
                    }
                    uiState.transaction != null -> {
                        TransactionDetailsContent(
                            transaction = uiState.transaction!!,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailsContent(
    transaction: MyTransactionListItem,
    viewModel: TransactionDetailsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Store Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Store Logo
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = getStoreColorForCategory(transaction.category_name)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (transaction.store_logo != null) {
                            AsyncImage(
                                model = transaction.store_logo,
                                contentDescription = transaction.store_name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = transaction.store_name.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Store Name
                Text(
                    text = transaction.store_name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.PrimaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Transaction Details
                TransactionDetailRow(
                    label = "Total Bill Amount",
                    value = "INR ${viewModel.formatAmount(transaction.bill_amount)}"
                )

                TransactionDetailRow(
                    label = "Total Bill Paid",
                    value = "INR ${viewModel.formatAmount(transaction.final_amount)}"
                )

                TransactionDetailRow(
                    label = "Amount Saved",
                    value = "INR ${viewModel.calculateTotalSavings(transaction)}"
                )

                TransactionDetailRow(
                    label = "Cashback Discount",
                    value = "INR ${viewModel.formatAmount(transaction.cashback_used)}"
                )

                TransactionDetailRow(
                    label = "Vendor's Discount",
                    value = "INR ${viewModel.formatAmount(transaction.vendor_discount)}"
                )

                TransactionDetailRow(
                    label = "Cashback Earned",
                    value = "INR ${viewModel.formatAmount(transaction.cashback_earned)}"
                )

                TransactionDetailRow(
                    label = "Transaction ID",
                    value = transaction.transaction_number
                )

                TransactionDetailRow(
                    label = "Date and Time",
                    value = viewModel.formatDateTime(transaction.created_at),
                    isLast = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Need Help Button
        Button(
            onClick = { /* Handle help action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlashColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Need Help? Click Here",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.SecondaryText,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!isLast) {
            Spacer(modifier = Modifier.height(16.dp))
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
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Retry",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

// Helper function
private fun getStoreColorForCategory(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "food" -> Color(0xFFE91E63) // Pink
        "grocery" -> Color(0xFF4CAF50) // Green
        "fashion" -> Color(0xFF9C27B0) // Purple
        "health" -> Color(0xFF2196F3) // Blue
        "electronics" -> Color(0xFFFF9800) // Orange
        else -> SlashColors.Primary
    }
}