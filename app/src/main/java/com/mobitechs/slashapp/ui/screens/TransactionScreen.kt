package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.ui.components.SlashButton
import com.mobitechs.slashapp.ui.components.SlashTopAppBar
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.TransactionViewModel
import com.mobitechs.slashapp.utils.formatDecimalString

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
        SlashTopAppBar(
            onBackClick = onBackClick, title = "Scan & Pay", modifier = Modifier
                .fillMaxWidth()
                .background(SlashColors.Primary)
                .statusBarsPadding()
                .padding(16.dp)
        )


        // Transaction Form
        LazyColumn(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Store Details Card - Updated with background image and bags
                uiState.storeDetails?.let { storeWithCategory ->
                    StoreDetailsCard(
                        store = storeWithCategory,
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
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
                    onCashbackChange = viewModel::onCashbackChange,
                    billAmount = uiState.billAmount.toDoubleOrNull() ?: 0.0
                )
            }

            item {
                CouponSection(
                    couponCode = uiState.couponCode,
                    onCouponChange = viewModel::onCouponChange,
                    onApplyCoupon = viewModel::applyCoupon,
                    onRemoveCoupon = viewModel::removeCoupon,
                    isApplied = uiState.isCouponApplied,
                    error = uiState.couponError
                )
            }

            item {
                BillingSummarySection(
                    storeName = uiState.storeDetails?.name ?: "ABC Grocery Store",
                    billAmount = uiState.billAmount.toDoubleOrNull() ?: 0.0,
                    vendorDiscount = uiState.vendorDiscount,
                    cashbackUsed = uiState.enteredCashback,
                    couponDiscount = uiState.couponDiscount,
                    tax = uiState.tax,
                    grandTotal = uiState.grandTotal,
                    isVendorDiscountApplicable = uiState.isVendorDiscountApplicable,
                    minimumOrderAmount = uiState.storeDetails?.minimum_order_amount?.toDoubleOrNull() ?: 0.0
                )
                Spacer(modifier = Modifier.height(30.dp))
            }

        }

        SlashButton(
            text = "PAY ₹${String.format("%.2f", uiState.grandTotal)}",
            onClick = viewModel::processPayment,
            isLoading = uiState.isLoading,
            enabled = uiState.isPayButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlashColors.OrangeBg)
    ) {
        // Background Image
//        Image(
//            painter = painterResource(id = R.drawable.discount_bg),
//            contentDescription = null,
//            modifier = Modifier.fillMaxWidth(),
//            contentScale = ContentScale.FillWidth
//        )


        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Changed from CenterVertically to Top
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.width(200.dp) // Add fixed width to control card width
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "VENDOR DISCOUNT",
                            color = SlashColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .width(200.dp) // Match the card width
                                .height(1.dp)
                                .drawBehind {
                                    val pathEffect =
                                        PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
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
                            text = "Total Vendor Discount: ${formatDecimalString(store.normal_discount_percentage)}%",
                            color = SlashColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Minimum Amount: ${store.minimum_order_amount}",
                            color = SlashColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Vendor Name: ${store.name}",
                            color = SlashColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                // Bags Image
                Image(
                    painter = painterResource(id = R.drawable.bags),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }


        }
    }
}

// 5. Transaction Form Components - No card, just divider
@Composable
private fun BillAmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    error: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Bill Amount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Filter to only allow numbers and decimal point
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                onAmountChange(filtered)
            },
            placeholder = { Text("Your total Bill Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SlashColors.Primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                focusedBorderColor = SlashColors.Primary,
//                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
//            )
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Divider
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun CashbackSection(
    availableCashback: Double,
    enteredCashback: Double,
    onCashbackChange: (Double) -> Unit,
    billAmount: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Available Cashback: ₹${String.format("%.2f", availableCashback)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Maximum Cashback | Enter Cashback Manually",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = if (enteredCashback > 0) enteredCashback.toString() else "",
            onValueChange = { newValue ->
                val amount = newValue.toDoubleOrNull() ?: 0.0
                val maxAllowed =
                    minOf(availableCashback, billAmount * 0.2) // 20% of bill or available cashback
                if (amount <= maxAllowed) {
                    onCashbackChange(amount)
                }
            },
            placeholder = { Text("₹ 0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SlashColors.Primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        Text(
            text = "NOTE: Up to 20% Cashback can be used in one order",
            fontSize = 12.sp,
            color = SlashColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Divider
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun CouponSection(
    couponCode: String,
    onCouponChange: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onRemoveCoupon: () -> Unit,
    isApplied: Boolean,
    error: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Apply Coupon",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = couponCode,
            onValueChange = onCouponChange,
            placeholder = { Text("Enter your coupon code") },
            modifier = Modifier.fillMaxWidth(),
            isError = error.isNotEmpty(),
            enabled = !isApplied,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isApplied) Color.Green else SlashColors.Primary,
                unfocusedBorderColor = if (isApplied) Color.Green else Color.Gray.copy(alpha = 0.5f)
            ),
            trailingIcon = {
                Row {
                    if (isApplied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Applied",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (couponCode.isNotEmpty()) {
                        if (isApplied) {
                            IconButton(
                                onClick = onRemoveCoupon,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = onApplyCoupon,
                                enabled = couponCode.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SlashColors.Primary
                                ),
                                modifier = Modifier
                                    .height(40.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "Apply",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Divider
        Divider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
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
    isVendorDiscountApplicable: Boolean, // Add this parameter
    minimumOrderAmount: Double = 0.0, // Add this parameter
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
            BillingSummaryRow(storeName, "")
            BillingSummaryRow("Bill Total", "₹${String.format("%.2f", billAmount)}")
            BillingSummaryRow("Tax", "₹${String.format("%.2f", tax)}")

            // Vendor discount with conditional display
            if (isVendorDiscountApplicable && vendorDiscount > 0) {
                BillingSummaryRow("Vendor Discount", "- ₹${String.format("%.2f", vendorDiscount)}")
            } else if (!isVendorDiscountApplicable && minimumOrderAmount > 0) {
                // Show message about minimum order requirement
                Text(
                    text = "* Vendor discount applicable on orders above ₹${String.format("%.2f", minimumOrderAmount)}",
                    fontSize = 12.sp,
                    color = SlashColors.OrangeBg,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (cashbackUsed > 0) {
                BillingSummaryRow("Cashback", "- ₹${String.format("%.2f", cashbackUsed)}")
            }

            if (couponDiscount > 0) {
                BillingSummaryRow("Coupon Discount", "- ₹${String.format("%.2f", couponDiscount)}")
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