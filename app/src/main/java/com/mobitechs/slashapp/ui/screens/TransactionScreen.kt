package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.ui.components.LoadingOverlay
import com.mobitechs.slashapp.ui.components.SlashButton
import com.mobitechs.slashapp.ui.components.SlashTopAppBar
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.ToastObserver
import com.mobitechs.slashapp.ui.viewmodels.TransactionViewModel
import com.mobitechs.slashapp.utils.formatDecimalString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    storeId: Int,
    viewModel: TransactionViewModel,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Observe toast events
    ToastObserver(viewModel = viewModel)

    LaunchedEffect(storeId) {
        viewModel.loadStoreDetails(storeId)
    }

    // Handle navigation to payment
    LaunchedEffect(uiState.navigateToPayment) {
        if (uiState.navigateToPayment) {
            navController.navigate(Screen.HomeScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = true }
            }
            viewModel.onNavigateToPayment()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlashColors.Primary,
                shadowElevation = 4.dp
            ) {
                SlashTopAppBar(
                    onBackClick = onBackClick,
                    title = "Scan & Pay",
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                )
            }

            // Transaction Form
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    uiState.storeDetails?.let { storeWithCategory ->
                        StoreDetailsCard(store = storeWithCategory)
                    }
                }

                item {
                    BillAmountSection(
                        amount = uiState.billAmount,
                        onAmountChange = viewModel::onBillAmountChange,
                        error = uiState.billAmountError
                    )
                }

                item {
                    CashbackSection(
                        availableCashback = uiState.availableCashback,
                        enteredCashback = uiState.enteredCashback,
                        maxAllowedCashback = uiState.maxAllowedCashback,
                        onCashbackChange = viewModel::onCashbackChange,
                        onMaxCashbackClick = viewModel::onMaxCashbackClick,
                        onClearCashback = viewModel::onClearCashback,
                        billAmount = uiState.billAmount.toDoubleOrNull() ?: 0.0,
                        cashbackPercentage = uiState.cashbackPercentage,
                        isProfileLoading = uiState.isProfileLoading,
                        onRefreshWallet = viewModel::refreshWalletDetails
                    )
                }

                item {
                    CouponSection(
                        couponCode = uiState.couponCode,
                        onCouponChange = viewModel::onCouponChange,
                        onApplyCoupon = viewModel::applyCoupon,
                        onRemoveCoupon = viewModel::removeCoupon,
                        isApplied = uiState.isCouponApplied,
                        appliedCouponDetails = uiState.appliedCouponDetails,
                        couponDiscount = uiState.couponDiscount,
                        error = uiState.couponError,
                        isLoading = uiState.isCouponLoading
                    )
                }

                item {
                    BillingSummarySection(
                        storeName = uiState.storeDetails?.name ?: "Store",
                        billAmount = uiState.billAmount.toDoubleOrNull() ?: 0.0,
                        vendorDiscount = uiState.vendorDiscount,
                        cashbackUsed = uiState.enteredCashback,
                        couponDiscount = uiState.couponDiscount,
                        tax = uiState.tax,
                        grandTotal = uiState.grandTotal,
                        totalSavings = uiState.totalSavings,
                        isVendorDiscountApplicable = uiState.isVendorDiscountApplicable,
                        minimumOrderAmount = uiState.storeDetails?.minimum_order_amount?.toDoubleOrNull() ?: 0.0
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Pay Button
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (uiState.totalSavings > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SlashColors.SuccessGreen.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = SlashColors.SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎉 You're saving ₹${String.format("%.2f", uiState.totalSavings)} on this order!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlashColors.SuccessGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SlashButton(
                        text = "PAY ₹${String.format("%.2f", uiState.grandTotal)}",
                        onClick = viewModel::processPayment,
                        isLoading = uiState.isLoading,
                        enabled = uiState.isPayButtonEnabled,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Loading overlay
        LoadingOverlay(isVisible = uiState.isLoading)

        // Error handling
        if (uiState.error.isNotEmpty()) {
            LaunchedEffect(uiState.error) {
                // Auto-clear error after showing
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun StoreDetailsCard(
    store: StoreListItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlashColors.OrangeBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.width(220.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Percent,
                                contentDescription = null,
                                tint = SlashColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VENDOR DISCOUNT",
                                color = SlashColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .drawBehind {
                                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    drawLine(
                                        color = SlashColors.DottedLineColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        pathEffect = pathEffect,
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Discount: ${formatDecimalString(store.normal_discount_percentage)}%",
                            color = SlashColors.Primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Min. Order: ₹${formatDecimalString(store.minimum_order_amount)}",
                            color = SlashColors.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = store.name,
                            color = SlashColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.bags),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

@Composable
private fun BillAmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wallet,
                    contentDescription = null,
                    tint = SlashColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bill Amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        onAmountChange(filtered)
                    }
                },
                placeholder = {
                    Text(
                        "Enter your total bill amount",
                        color = SlashColors.TextHint
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = error.isNotEmpty(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SlashColors.Primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    errorBorderColor = SlashColors.InputBorderError
                ),
                prefix = {
                    Text(
                        text = "₹ ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.TextPrimary
                    )
                }
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CashbackSection(
    availableCashback: Double,
    enteredCashback: Double,
    maxAllowedCashback: Double,
    onCashbackChange: (String) -> Unit,
    onMaxCashbackClick: () -> Unit,
    onClearCashback: () -> Unit,
    billAmount: Double,
    cashbackPercentage: String,
    isProfileLoading: Boolean,
    onRefreshWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track if user wants to use max cashback
    var useMaxCashback by remember { mutableStateOf(false) }

    // Update checkbox state when cashback changes
    LaunchedEffect(enteredCashback, maxAllowedCashback) {
        useMaxCashback = enteredCashback == maxAllowedCashback && maxAllowedCashback > 0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = SlashColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wallet Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.TextPrimary
                    )
                }

                IconButton(
                    onClick = onRefreshWallet,
                    enabled = !isProfileLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isProfileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SlashColors.Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh wallet",
                            tint = SlashColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Available Cashback Display
            Card(
                colors = CardDefaults.cardColors(containerColor = SlashColors.Primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Cashback",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.TextSecondary
                    )
                    Text(
                        text = "₹${String.format("%.2f", availableCashback)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlashColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Max usable info
            Card(
                colors = CardDefaults.cardColors(containerColor = SlashColors.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SlashColors.TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Max usable: ₹${String.format("%.2f", maxAllowedCashback)} (20% of bill)",
                        fontSize = 12.sp,
                        color = SlashColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cashback input section
            Text(
                text = "Use Cashback",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Cashback input field
            OutlinedTextField(
                value = if (enteredCashback > 0) String.format("%.2f", enteredCashback) else "",
                onValueChange = { newValue ->
                    useMaxCashback = false // Uncheck when user manually enters value
                    onCashbackChange(newValue)
                },
                placeholder = { Text("0.00", color = SlashColors.TextHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !useMaxCashback, // Disable when max checkbox is checked
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SlashColors.Primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledBorderColor = SlashColors.Primary.copy(alpha = 0.5f)
                ),
                prefix = {
                    Text(
                        text = "₹ ",
                        fontSize = 14.sp,
                        color = SlashColors.TextPrimary
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Use Max Cashback Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useMaxCashback,
                    onCheckedChange = { checked ->
                        useMaxCashback = checked
                        if (checked) {
                            onMaxCashbackClick()
                        } else {
                            onClearCashback()
                        }
                    },
                    enabled = maxAllowedCashback > 0,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SlashColors.Primary,
                        uncheckedColor = SlashColors.InputBorder,
                        checkmarkColor = SlashColors.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Use maximum cashback (₹${String.format("%.2f", maxAllowedCashback)})",
                    fontSize = 14.sp,
                    color = if (maxAllowedCashback > 0) SlashColors.TextPrimary else SlashColors.TextHint,
                    fontWeight = FontWeight.Medium
                )
            }

            if (enteredCashback > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Using $cashbackPercentage of bill amount",
                    fontSize = 12.sp,
                    color = SlashColors.Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CouponSection(
    couponCode: String,
    onCouponChange: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onRemoveCoupon: () -> Unit,
    isApplied: Boolean,
    appliedCouponDetails: String,
    couponDiscount: Double,
    error: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = SlashColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Apply Coupon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show applied coupon details when coupon is applied
            if (isApplied && appliedCouponDetails.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlashColors.SuccessGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SlashColors.SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Coupon Applied!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlashColors.SuccessGreen
                                )
                            }
                            Text(
                                text = appliedCouponDetails,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlashColors.TextPrimary
                            )
                            Text(
                                text = "Discount: ₹${String.format("%.2f", couponDiscount)}",
                                fontSize = 12.sp,
                                color = SlashColors.SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = onRemoveCoupon,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Only show input field when coupon is not applied
            if (!isApplied) {
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = onCouponChange,
                    placeholder = {
                        Text(
                            "Enter your coupon code",
                            color = SlashColors.TextHint
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error.isNotEmpty(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlashColors.Primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        errorBorderColor = SlashColors.InputBorderError
                    ),
                    trailingIcon = {
                        if (couponCode.isNotEmpty()) {
                            Button(
                                onClick = onApplyCoupon,
                                enabled = !isLoading && couponCode.length >= 3,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SlashColors.Primary
                                ),
                                modifier = Modifier
                                    .height(40.dp)
                                    .padding(end = 4.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Apply",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BillingSummarySection(
    storeName: String,
    billAmount: Double,
    vendorDiscount: Double,
    cashbackUsed: Double,
    couponDiscount: Double,
    tax: Double,
    grandTotal: Double,
    totalSavings: Double,
    isVendorDiscountApplicable: Boolean,
    minimumOrderAmount: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Billing Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SlashColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Store name
            BillingSummaryRow(storeName, "", isHeader = true)

            Spacer(modifier = Modifier.height(8.dp))

            BillingSummaryRow("Bill Total", "₹${String.format("%.2f", billAmount)}")
            BillingSummaryRow("Tax (2.72%)", "₹${String.format("%.2f", tax)}")

            // Discounts section
            if (vendorDiscount > 0 || cashbackUsed > 0 || couponDiscount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = SlashColors.SectionDivider)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Discounts Applied",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Vendor discount with conditional display
            if (isVendorDiscountApplicable && vendorDiscount > 0) {
                BillingSummaryRow(
                    "Vendor Discount",
                    "- ₹${String.format("%.2f", vendorDiscount)}",
                    textColor = SlashColors.SuccessGreen
                )
            } else if (!isVendorDiscountApplicable && minimumOrderAmount > 0 && billAmount > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlashColors.WarningOrange.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "💡 Add ₹${String.format("%.2f", minimumOrderAmount - billAmount)} more to get vendor discount",
                        fontSize = 12.sp,
                        color = SlashColors.WarningOrange,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (cashbackUsed > 0) {
                BillingSummaryRow(
                    "Cashback Used",
                    "- ₹${String.format("%.2f", cashbackUsed)}",
                    textColor = SlashColors.SuccessGreen
                )
            }

            if (couponDiscount > 0) {
                BillingSummaryRow(
                    "Coupon Discount",
                    "- ₹${String.format("%.2f", couponDiscount)}",
                    textColor = SlashColors.SuccessGreen
                )
            }

            // Show total savings if any
            if (totalSavings > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlashColors.SuccessGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Savings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlashColors.SuccessGreen
                            )
                        }
                        Text(
                            text = "₹${String.format("%.2f", totalSavings)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlashColors.SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SlashColors.InputBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Grand total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grand Total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.TextPrimary
                )
                Text(
                    text = "₹${String.format("%.2f", grandTotal)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.Primary
                )
            }
        }
    }
}

@Composable
private fun BillingSummaryRow(
    label: String,
    value: String,
    textColor: Color = SlashColors.TextPrimary,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (isHeader) 0.dp else 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isHeader) 16.sp else 14.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) SlashColors.TextPrimary else SlashColors.TextSecondary
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = if (isHeader) 16.sp else 14.sp,
                color = textColor,
                fontWeight = if (textColor == SlashColors.SuccessGreen) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}