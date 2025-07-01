package com.mobitechs.slashapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobitechs.slashapp.ui.components.LoadingOverlay
import com.mobitechs.slashapp.ui.components.PhoneNumberInputField
import com.mobitechs.slashapp.ui.components.SlashButton
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.AuthPhoneViewModel

@Composable
fun AuthPhoneScreen(
    viewModel: AuthPhoneViewModel,
    onNavigateToOtp: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation
    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            onNavigateToOtp(uiState.phoneNumber)
            viewModel.onNavigateToOtp()
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
                    .background(SlashColors.Background)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    // Illustration Container
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                color = SlashColors.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(
                                    color = SlashColors.Primary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔐📱",
                                fontSize = 64.sp,
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
                    // Welcome text
                    Text(
                        text = "Welcome!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlashColors.TextPrimary,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = "Enter your phone number, we will send you OTP to verify",
                        fontSize = 16.sp,
                        color = SlashColors.TextSecondary,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Phone number input
                    PhoneNumberInputField(
                        phoneNumber = uiState.phoneNumber,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        hasError = uiState.hasError,
                        isValid = uiState.isValidPhone,
                        errorMessage = uiState.phoneError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Get OTP button
                    SlashButton(
                        text = "Get OTP",
                        onClick = viewModel::sendOtp,
                        isLoading = uiState.isLoading,
                        enabled = uiState.isValidPhone && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(120.dp))  // Bottom margin for button
                }
            }
        }

        // Loading overlay
        LoadingOverlay(isVisible = uiState.isLoading)

        // Error message
        if (uiState.error.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

//@Composable
//fun PhoneInputScreen(
//    onNavigateToOtp: (String) -> Unit,
//    viewModel: PhoneInputViewModel = viewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//
//    // Handle navigation
//    LaunchedEffect(uiState.navigateToOtp) {
//        if (uiState.navigateToOtp) {
//            onNavigateToOtp(uiState.phoneNumber)
//            viewModel.onNavigateToOtp()
//        }
//    }
//
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Top section with cream background and illustration
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(SlashColors.Background)
//                    .padding(horizontal = 24.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Spacer(modifier = Modifier.height(60.dp))
//
//                    // Illustration Container
//                    Box(
//                        modifier = Modifier
//                            .size(220.dp)
//                            .background(
//                                color = SlashColors.White,
//                                shape = CircleShape
//                            ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(180.dp)
//                                .background(
//                                    color = SlashColors.Primary.copy(alpha = 0.1f),
//                                    shape = CircleShape
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "🔐📱",
//                                fontSize = 64.sp,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(40.dp))
//                }
//            }
//
//            // Bottom section - Edge to edge white card
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(), // Fill remaining height
//                colors = CardDefaults.cardColors(containerColor = SlashColors.White),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(
//                    topStart = 16.dp,
//                    topEnd = 16.dp,
//                    bottomStart = 0.dp,  // No rounding at bottom
//                    bottomEnd = 0.dp    // No rounding at bottom
//                ),
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//            ) {
//
//                Spacer(modifier = Modifier.height(20.dp))
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(
//                            start = 24.dp,   // Only internal content padding
//                            end = 24.dp,     // Only internal content padding
//                            top = 24.dp,     // Only internal content padding
//                            bottom = 0.dp    // No bottom padding - edge to edge
//                        ),
//
//                ) {
//                    // Welcome text
//                    Text(
//                        text = "Welcome!",
//                        fontSize = 32.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = SlashColors.TextPrimary,
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // Subtitle
//                    Text(
//                        text = "Enter your phone number, we will send you OTP to verify",
//                        fontSize = 16.sp,
//                        color = SlashColors.TextSecondary,
//                        lineHeight = 22.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(32.dp))
//
//                    // Phone number input
//                    PhoneNumberInputField(
//                        phoneNumber = uiState.phoneNumber,
//                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
//                        hasError = uiState.hasError,
//                        isValid = uiState.isValidPhone,
//                        errorMessage = uiState.phoneError,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    // Get OTP button
//                    SlashButton(
//                        text = "Get OTP",
//                        onClick = viewModel::sendOtp,
//                        isLoading = uiState.isLoading,
//                        enabled = uiState.isValidPhone && !uiState.isLoading,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//
//                    Spacer(modifier = Modifier.height(120.dp))  // Bottom margin for button
//                }
//            }
//        }
//
//        // Loading overlay
//        LoadingOverlay(isVisible = uiState.isLoading)
//    }
//}
