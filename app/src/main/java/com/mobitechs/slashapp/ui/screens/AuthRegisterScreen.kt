package com.mobitechs.slashapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.ui.components.LoadingOverlay
import com.mobitechs.slashapp.ui.components.PhoneNumberInputField
import com.mobitechs.slashapp.ui.components.RegularSmallText
import com.mobitechs.slashapp.ui.components.SlashButton
import com.mobitechs.slashapp.ui.components.SlashTopAppBar
import com.mobitechs.slashapp.ui.components.TermsAndConditionsCheckbox
import com.mobitechs.slashapp.ui.components.ValidatedInputField
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.AuthRegisterViewModel


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthRegisterScreen(
    viewModel: AuthRegisterViewModel,
    navController: NavController,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation
    LaunchedEffect(uiState.navigateToOtp) {
        if (uiState.navigateToOtp) {
            navController.navigate(Screen.HomeScreen.route)
            viewModel.onNavigateToOtp()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background with gradient - 25% colored, 75% white
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenHeight = maxHeight
            val gradientHeight = screenHeight * 0.4f // 25% of screen

            // Gradient background for top 25%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SlashColors.Background,
                                SlashColors.Background.copy(alpha = 0.8f),
                                SlashColors.White
                            ),
                            startY = 0f,
                            endY = gradientHeight.value
                        )
                    )
            )

            // White background for rest 75%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = gradientHeight)
                    .background(SlashColors.White)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
            ) {
                SlashTopAppBar(onBackClick = onBackClick, title = "Sign Up")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, end = 25.dp, start = 25.dp)
            ) {
                RegularSmallText(title = "Fill all the details to create your account")

                Spacer(modifier = Modifier.height(32.dp))

                ValidatedInputField(
                    title = "First Name",
                    placeholder = "John",
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    validation = uiState.firstNameValidation,
                    keyboardType = KeyboardType.Text,
                    maxLength = 20,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                ValidatedInputField(
                    title = "Last Name",
                    placeholder = "Doe",
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    validation = uiState.lastNameValidation,
                    keyboardType = KeyboardType.Text,
                    maxLength = 40,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                PhoneNumberInputField(
                    phoneNumber = uiState.phoneNumber,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    hasError = !uiState.phoneValidation.isValid && uiState.phoneNumber.isNotEmpty(),
                    isValid = uiState.phoneValidation.isValid,
                    errorMessage = uiState.phoneValidation.errorMessage,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                ValidatedInputField(
                    title = "Email",
                    placeholder = "john@gmail.com",
                    value = uiState.emailId,
                    onValueChange = viewModel::onEmailIdChange,
                    validation = uiState.emailValidation,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                ValidatedInputField(
                    title = "Referral Code",
                    placeholder = "Enter referral code",
                    value = uiState.referralCode,
                    onValueChange = viewModel::onReferralCodeChange,
                    validation = uiState.referralCodeValidation,
                    keyboardType = KeyboardType.Text,
                    maxLength = 10,
                    isRequired = false, // Optional field
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))
                TermsAndConditionsCheckbox(
                    isChecked = uiState.isTermsAccepted,
                    onCheckedChange = viewModel::onTermsAcceptedChange,
                    onTermsClick = {
                        // Navigate to Terms & Conditions screen
                        // navController.navigate(Screen.TermsAndConditionsScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))
                SlashButton(
                    text = "Signup",
                    onClick = viewModel::registerAPICall,
                    isLoading = uiState.isLoading,
                    enabled = uiState.isFormValid && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Loading overlay
        LoadingOverlay(isVisible = uiState.isLoading)

        // Error message
        if (uiState.error.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
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