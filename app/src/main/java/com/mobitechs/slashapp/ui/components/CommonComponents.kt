package com.mobitechs.slashapp.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.utils.ValidationResult


@Composable
fun SlashButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) SlashColors.ButtonBackground else SlashColors.ButtonDisabled,
            contentColor = SlashColors.White,
            disabledContainerColor = SlashColors.ButtonDisabled,
            disabledContentColor = SlashColors.TextSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = SlashColors.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.White
            )
        }
    }
}

@Composable
fun PhoneNumberInputField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    hasError: Boolean = false,
    isValid: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Label
        Text(
            text = "Phone Number",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = SlashColors.TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Single combined input box for country code and phone number
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        hasError -> SlashColors.InputBackgroundError
                        else -> SlashColors.White  // Always white background unless error
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = when {
                        hasError -> SlashColors.InputBorderError  // Red only when hasError is true
                        isValid -> SlashColors.Primary  // Green when valid
                        else -> SlashColors.InputBorder  // Default gray
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country Code (part of same input)
            Text(
                text = "(+91)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.TextHint
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Phone Number Input
            BasicTextField(
                value = phoneNumber,
                onValueChange = { value ->
                    // Filter only digits and limit to 10
                    val filtered = value.filter { it.isDigit() }
                    if (filtered.length <= 10) {
                        onPhoneNumberChange(filtered)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = SlashColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (phoneNumber.isEmpty()) {
                        Text(
                            text = "0004145230",
                            fontSize = 16.sp,
                            color = SlashColors.TextHint
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Error Message - Fixed to show properly
        if (hasError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = SlashColors.TextError,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}


@Composable
fun RegularSmallText(
    title : String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = SlashColors.TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun RegularMediumText(
    title : String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = SlashColors.TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun RegularLargeText(
    title : String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = SlashColors.TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
//
//@Composable
//fun RegularInputField(
//    title : String,
//    placeHolder : String,
//    actualValue: String,
//    actualValueChange: (String) -> Unit,
//    hasError: Boolean = false,
//    isValid: Boolean = false,
//    errorMessage: String = "",
//    modifier: Modifier = Modifier
//) {
//    Column(modifier = modifier) {
//        // Label
//        Text(
//            text = title,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium,
//            color = SlashColors.TextSecondary,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        // Single combined input box for country code and phone number
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(
//                    when {
//                        hasError -> SlashColors.InputBackgroundError
//                        else -> SlashColors.White  // Always white background unless error
//                    }
//                )
//                .border(
//                    width = 1.5.dp,
//                    color = when {
//                        hasError -> SlashColors.InputBorderError  // Red only when hasError is true
//                        isValid -> SlashColors.Primary  // Green when valid
//                        else -> SlashColors.InputBorder  // Default gray
//                    },
//                    shape = RoundedCornerShape(12.dp)
//                )
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            BasicTextField(
//                value = actualValue,
//                onValueChange = { value ->
//                    // Filter only digits and limit to 10
//                    val filtered = value.filter { it.isDigit() }
//                    if (filtered.length <= 10) {
//                        actualValueChange(filtered)
//                    }
//                },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                textStyle = TextStyle(
//                    fontSize = 16.sp,
//                    color = SlashColors.TextPrimary,
//                    fontWeight = FontWeight.Medium
//                ),
//                modifier = Modifier.weight(1f),
//                singleLine = true,
//                decorationBox = { innerTextField ->
//                    if (actualValue.isEmpty()) {
//                        Text(
//                            text = placeHolder,
//                            fontSize = 16.sp,
//                            color = SlashColors.TextHint
//                        )
//                    }
//                    innerTextField()
//                }
//            )
//        }
//
//        // Error Message - Fixed to show properly
//        if (hasError && errorMessage.isNotEmpty()) {
//            Text(
//                text = errorMessage,
//                color = SlashColors.TextError,
//                fontSize = 14.sp,
//                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
//            )
//        }
//    }
//}

@Composable
fun ValidatedInputField(
    title: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    validation: ValidationResult,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = Int.MAX_VALUE,
    isRequired: Boolean = true
) {
    Column(modifier = modifier) {
        // Label with required indicator
        Row {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isRequired) {
                Text(
                    text = " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SlashColors.TextError,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        !validation.isValid && value.isNotEmpty() -> SlashColors.InputBackgroundError
                        else -> SlashColors.White
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = when {
                        !validation.isValid && value.isNotEmpty() -> SlashColors.InputBorderError
                        validation.isValid && value.isNotEmpty() -> SlashColors.Primary
                        else -> SlashColors.InputBorder
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.length <= maxLength) {
                        onValueChange(newValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = SlashColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = SlashColors.TextHint
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Error Message
        if (!validation.isValid && value.isNotEmpty()) {
            Text(
                text = validation.errorMessage,
                color = SlashColors.TextError,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

@Composable
fun TermsAndConditionsCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SlashColors.Primary,
                uncheckedColor = SlashColors.InputBorder,
                checkmarkColor = SlashColors.White
            ),
            modifier = Modifier.padding(end = 4.dp)
        )

        Text(
            text = buildAnnotatedString {
                append("I agree to ")
                withStyle(
                    style = SpanStyle(
                        color = SlashColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Terms & Conditions")
                }
            },
            fontSize = 14.sp,
            color = SlashColors.TextSecondary,
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable { onTermsClick() }
        )
    }
}

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    hasError: Boolean = false,
    isValid: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Visible OTP boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            repeat(otpLength) { index ->
                val char = if (index < otpValue.length) otpValue[index].toString() else ""
                val isFocused = index == otpValue.length

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                hasError -> SlashColors.InputBackgroundError
                                else -> SlashColors.White
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = when {
                                hasError -> SlashColors.InputBorderError
                                isValid && otpValue.length == otpLength -> SlashColors.Primary
                                isFocused -> SlashColors.Primary
                                else -> SlashColors.InputBorder
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
//                            color = SlashColors.TextPrimary
                            color = when {
                                hasError -> SlashColors.InputBorderError
                                else -> SlashColors.TextPrimary
                            },

                        )
                    )
                }
            }
        }

        // Invisible text field that covers the entire Row area
        BasicTextField(
            value = otpValue,
            onValueChange = { value ->
                if (value.length <= otpLength && value.all { it.isDigit() }) {
                    onOtpChange(value)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .matchParentSize() // Match the size of the Row
                .alpha(0.01f), // Almost invisible but still focusable
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = 1.sp // Very small text
            ),
            cursorBrush = SolidColor(Color.Transparent), // Hide cursor
            singleLine = true
        )
    }
}

@Composable
fun SlashTopAppBar(
    title: String = "",
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = SlashColors.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (title.isNotEmpty()) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.TextPrimary,
                modifier = Modifier.padding(start = if (onBackClick != null) 8.dp else 0.dp)
            )
        }
    }
}

@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlashColors.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SlashColors.Primary,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}