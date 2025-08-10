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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.TextStyle
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
import com.mobitechs.slashapp.data.model.MyTransactionListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuTransactionScreen(
    viewModel: BottomMenuTransactionViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
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
                viewModel.loadMoreTransactions()
            }
        }
    }

    // Group transactions by date
    val groupedTransactions = remember(uiState.transactions) {
        uiState.transactions.groupBy { transaction ->
            getDateFromTransaction(transaction.created_at)
        }.toList().sortedByDescending { it.first }
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
                        text = "Transactions",
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
                    // Search Bar Section
                    item(key = "search_section") {
                        SearchDateBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }

                    // Transaction List Content
                    when {
                        uiState.transactionsError.isNotEmpty() -> {
                            item(key = "error_section") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    ErrorMessage(
                                        message = uiState.transactionsError,
                                        onRetry = { viewModel.retryLoadTransactions() }
                                    )
                                }
                            }
                        }
                        uiState.isTransactionsLoading -> {
                            item(key = "loading_section") {
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
                        uiState.transactions.isEmpty() -> {
                            item(key = "empty_section") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyMessage(
                                        message = if (uiState.searchQuery.isNotEmpty()) {
                                            "No transactions found for \"${uiState.searchQuery}\""
                                        } else {
                                            "No transactions available"
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            groupedTransactions.forEachIndexed { groupIndex, (date, transactions) ->
                                // Date Header
                                item(key = "date_header_$date") {
                                    Text(
                                        text = formatDateHeader(date),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SlashColors.PrimaryText,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }

                                // Transactions for this date
                                itemsIndexed(
                                    items = transactions,
                                    key = { index, transaction -> "transaction_${transaction.id}_${groupIndex}_${index}" }
                                ) { index, transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        viewModel = viewModel,
                                        onTransactionClick = { transactionId ->
                                            navController.navigate("${Screen.TransactionDetailsScreen.route}/${transactionId}")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Loading more indicator
                            if (uiState.isLoadingMore) {
                                item(key = "loading_more") {
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

@Composable
private fun SearchDateBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(
                    Color(0xFFF5F5F5),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SlashColors.SecondaryText,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = SlashColors.PrimaryText
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search by date, card, vendor",
                            fontSize = 14.sp,
                            color = SlashColors.SecondaryText
                        )
                    }
                    innerTextField()
                }
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChanged("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = SlashColors.SecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Filter Button
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clickable { /* Handle filter click */ },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filter",
                    tint = SlashColors.SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: MyTransactionListItem,
    viewModel: BottomMenuTransactionViewModel,
    onTransactionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onTransactionClick(transaction.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Store Logo
        Surface(
            modifier = Modifier.size(40.dp),
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Transaction Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Store Name and Total Bill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.store_name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.PrimaryText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Total Bill: ${viewModel.formatAmount(transaction.bill_amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.PrimaryText
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Total Discount and Total Bill Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Discount: ${calculateTotalDiscount(transaction)}",
                    fontSize = 12.sp,
                    color = SlashColors.SuccessGreen,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Total Bill Paid: ${viewModel.formatAmount(transaction.final_amount)}",
                    fontSize = 12.sp,
                    color = SlashColors.SecondaryText
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
                contentDescription = "No transactions",
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

// Helper functions
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

private fun calculateTotalDiscount(transaction: MyTransactionListItem): String {
    return try {
        val billAmount = transaction.bill_amount.toDouble()
        val finalAmount = transaction.final_amount.toDouble()
        val discount = billAmount - finalAmount
        String.format("%.0f", discount)
    } catch (e: Exception) {
        "0"
    }
}

private fun getDateFromTransaction(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString.split("T")[0]
    }
}

private fun formatDateHeader(dateString: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transactionDate = dateFormat.parse(dateString)
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val transactionCalendar = Calendar.getInstance().apply { time = transactionDate ?: Date() }

        when {
            isSameDay(today, transactionCalendar) -> "Today, ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionDate)}"
            isSameDay(yesterday, transactionCalendar) -> "Yesterday, ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionDate)}"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transactionDate ?: Date())
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}