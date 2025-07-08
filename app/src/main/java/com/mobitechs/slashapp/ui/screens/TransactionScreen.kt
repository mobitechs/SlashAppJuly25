package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.TransactionViewModel

// 9. Updated TransactionScreen with navigation handling
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    storeId: Int,
    viewModel: TransactionViewModel,
    navController: NavController,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storeId) {
        viewModel.loadStoreDetails(storeId)
    }

    // Handle navigation to payment
    LaunchedEffect(uiState.navigateToPayment) {
        if (uiState.navigateToPayment) {
            // Navigate to payment success or payment gateway
            // For now, just show success and go back to home
            navController.navigate(Screen.HomeScreen.route) {
                popUpTo(Screen.HomeScreen.route) { inclusive = true }
            }
            viewModel.onNavigateToPayment()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlashColors.Primary)
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Scan & Pay",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Store Details Card
        uiState.storeDetails?.let { storeWithCategory ->
            StoreDetailsCard(
                store = storeWithCategory,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Transaction Form
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    selectedCashback = uiState.selectedCashback,
                    onCashbackChange = viewModel::onCashbackChange
                )
            }

            item {
                CouponSection(
                    couponCode = uiState.couponCode,
                    onCouponChange = viewModel::onCouponChange,
                    onApplyCoupon = viewModel::applyCoupon,
                    isApplied = uiState.isCouponApplied,
                    error = uiState.couponError
                )
            }

            item {
                BillingSummarySection(
                    billAmount = uiState.billAmount.toDoubleOrNull() ?: 0.0,
                    vendorDiscount = uiState.vendorDiscount,
                    cashbackUsed = uiState.selectedCashback,
                    couponDiscount = uiState.couponDiscount,
                    tax = uiState.tax,
                    grandTotal = uiState.grandTotal
                )
            }
        }

        // Pay Button
        PayButton(
            amount = uiState.grandTotal,
            enabled = uiState.isPayButtonEnabled,
            isLoading = uiState.isLoading,
            onClick = viewModel::processPayment,
            modifier = Modifier.padding(16.dp)
        )
    }

    // Error handling
    if (uiState.error.isNotEmpty()) {
        LaunchedEffect(uiState.error) {
            // Show error toast or snackbar
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6B35)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VENDOR DISCOUNT",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${store.normal_discount_percentage}%",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Vendor Name: ${store.name}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 5. Transaction Form Components
@Composable
private fun BillAmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "Bill Amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SlashColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Filter to only allow numbers and decimal point
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    onAmountChange(filtered)
                },
                label = { Text("Your total Bill Amount") },
                placeholder = { Text("₹0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = error.isNotEmpty(),
                singleLine = true
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CashbackSection(
    availableCashback: Double,
    selectedCashback: Double,
    onCashbackChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "Available Cashback: ₹${String.format("%.2f", availableCashback)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SlashColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = selectedCashback == availableCashback,
                    onCheckedChange = { isChecked ->
                        onCashbackChange(if (isChecked) availableCashback else 0.0)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SlashColors.Primary
                    )
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Maximum Cashback: Enter Cashback Manually",
                        fontSize = 14.sp,
                        color = SlashColors.TextPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Text(
                        text = "₹${String.format("%.2f", availableCashback)}",
                        fontSize = 12.sp,
                        color = SlashColors.TextSecondary,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            Text(
                text = "Note: Up to 20% Cashback can be used in one order",
                fontSize = 12.sp,
                color = SlashColors.TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CouponSection(
    couponCode: String,
    onCouponChange: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    isApplied: Boolean,
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "Apply Coupon",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SlashColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = onCouponChange,
                    label = { Text("Enter your coupon code") },
                    placeholder = { Text("SAVE20") },
                    modifier = Modifier.weight(1f),
                    isError = error.isNotEmpty(),
                    enabled = !isApplied,
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApplyCoupon,
                    enabled = couponCode.isNotEmpty() && !isApplied,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) SlashColors.Primary else SlashColors.Primary,
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isApplied) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isApplied) "Applied" else "Apply",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BillingSummarySection(
    billAmount: Double,
    vendorDiscount: Double,
    cashbackUsed: Double,
    couponDiscount: Double,
    tax: Double,
    grandTotal: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Billing Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Store name
            BillingSummaryRow("ABC Grocery Store", "")
            BillingSummaryRow("Bill Total", "₹${String.format("%.2f", billAmount)}")
            BillingSummaryRow("Tax", "₹${String.format("%.2f", tax)}")

            if (vendorDiscount > 0) {
                BillingSummaryRow("Vendor Discount", "-₹${String.format("%.2f", vendorDiscount)}")
            }

            if (cashbackUsed > 0) {
                BillingSummaryRow("Cashback", "-₹${String.format("%.2f", cashbackUsed)}")
            }

            if (couponDiscount > 0) {
                BillingSummaryRow("Coupon Discount", "-₹${String.format("%.2f", couponDiscount)}")
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = SlashColors.InputBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Grand Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.TextPrimary
                )
                Text(
                    text = "₹${String.format("%.2f", grandTotal)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BillingSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = SlashColors.TextSecondary
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = SlashColors.TextPrimary
            )
        }
    }
}

@Composable
private fun PayButton(
    amount: Double,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SlashColors.Primary,
            disabledContainerColor = SlashColors.ButtonDisabled
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "PAY ₹${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}