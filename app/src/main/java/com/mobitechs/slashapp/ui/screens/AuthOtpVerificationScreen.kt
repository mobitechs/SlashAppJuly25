package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.ui.viewmodels.AuthOtpVerificationViewModel
import com.mobitechs.slashapp.ui.components.*
import com.mobitechs.slashapp.ui.theme.SlashColors

@Composable
fun AuthOtpVerificationScreen(
    phoneNumber: String,
    otp: String,
    otpExpiry: String,
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: AuthOtpVerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()

    // Set phone number when screen opens
    LaunchedEffect(phoneNumber) {
        viewModel.setPhoneNumber(phoneNumber)
    }

    // Handle navigation
    LaunchedEffect(uiState.navigateToNext) {
        if (uiState.navigateToNext) {
//            onVerificationSuccess()
//            viewModel.onNavigateToNext()

            if(uiState.is_new_user){
                navController.navigate(Screen.AuthRegisterScreen.route)
            }else{
                navController.navigate(Screen.HomeScreen.route)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top section with cream background and illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlashColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 26.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        SlashTopAppBar(onBackClick = onBackClick)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Illustration Container
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                color = SlashColors.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(
                                    color = SlashColors.Primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔐",
                                fontSize = 56.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // Bottom section - Edge to edge white card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(), // Fill remaining height
                colors = CardDefaults.cardColors(containerColor = SlashColors.White),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,  // No rounding at bottom
                    bottomEnd = 0.dp    // No rounding at bottom
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 24.dp,   // Only internal content padding
                            end = 24.dp,     // Only internal content padding
                            top = 24.dp,     // Only internal content padding
                            bottom = 0.dp    // No bottom padding - edge to edge
                        ),
                ) {
                    // Title
                    Text(
                        text = "Phone Number Verification",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlashColors.TextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = "Enter 6 digits OTP (One Time Password) that sent to ${phoneNumber}. ($otp) Expires in $otpExpiry ",
                        fontSize = 16.sp,
                        color = SlashColors.TextSecondary,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Enter OTP Label
                    Text(
                        text = "Enter OTP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // OTP input
                    OtpInputField(
                        otpValue = uiState.otp,
                        onOtpChange = viewModel::onOtpChange,
                        hasError = uiState.hasError,
                        isValid = uiState.isValidOtp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error Message
                    if (uiState.hasError && uiState.otpError.isNotEmpty()) {
                        Text(
                            text = uiState.otpError,
                            color = SlashColors.TextError,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Verify button
                    SlashButton(
                        text = "Verify",
                        onClick = viewModel::verifyOtp,
                        isLoading = uiState.isLoading,
                        enabled = uiState.isValidOtp && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Resend OTP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (resendTimer > 0) "Resend OTP in ${resendTimer}s" else "Resend OTP",
                            fontSize = 16.sp,
                            color = if (resendTimer > 0) SlashColors.TextSecondary else SlashColors.Primary,
                            fontWeight = if (resendTimer > 0) FontWeight.Normal else FontWeight.Medium,
                            modifier = Modifier.clickable(enabled = resendTimer == 0) {
                                viewModel.resendOtp()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))  // Bottom margin for button
                }
            }
        }

        // Loading overlay
        LoadingOverlay(isVisible = uiState.isLoading)
    }
}