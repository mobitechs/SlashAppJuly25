package com.mobitechs.slashapp.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.data.model.CategoryItem
import com.mobitechs.slashapp.data.model.StoreListItem
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
    title: String,
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
    title: String,
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
    title: String,
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


/**
 * Common CategoryItem component that can be reused across different screens
 */
@Composable
fun CommonCategoryItem(
    category: CategoryItem,
    isSelected: Boolean = false,
    onSelected: (() -> Unit)? = null,
    getFallbackIconRes: (String) -> Int,
    modifier: Modifier = Modifier,
    size: CategoryItemSize = CategoryItemSize.Large
) {
    val itemWidth = when (size) {
        CategoryItemSize.Small -> 70.dp
        CategoryItemSize.Large -> 100.dp
    }

    val iconSize = when (size) {
        CategoryItemSize.Small -> 30.dp
        CategoryItemSize.Large -> 60.dp
    }

    val surfaceSize = when (size) {
        CategoryItemSize.Small -> 50.dp
        CategoryItemSize.Large -> 85.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .then(
                if (onSelected != null) {
                    Modifier.clickable { onSelected() }
                } else {
                    Modifier
                }
            )
            .width(itemWidth)
    ) {
        Surface(
            modifier = Modifier
                .size(surfaceSize)
                .then(
                    if (isSelected && size == CategoryItemSize.Small) {
                        Modifier.border(2.dp, SlashColors.Primary, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            shape = when (size) {
                CategoryItemSize.Small -> CircleShape
                CategoryItemSize.Large -> RoundedCornerShape(12.dp)
            },
            color = if (isSelected && size == CategoryItemSize.Small)
                SlashColors.Primary.copy(alpha = 0.1f) else Color.White,
            shadowElevation = if (isSelected) 4.dp else 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                if (category.name == "All") {
                    Icon(
                        painter = painterResource(id = getFallbackIconRes(category.name)),
                        contentDescription = category.name,
                        modifier = Modifier.size(iconSize),
                        tint = if (isSelected && size == CategoryItemSize.Small)
                            SlashColors.Primary else SlashColors.SecondaryText
                    )
                } else {
                    AsyncImage(
                        model = category.icon,
                        contentDescription = category.name,
                        modifier = Modifier.size(iconSize),
                        placeholder = painterResource(id = getFallbackIconRes(category.name)),
                        error = painterResource(id = getFallbackIconRes(category.name)),
                        fallback = painterResource(id = getFallbackIconRes(category.name)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = when (size) {
                CategoryItemSize.Small -> 10.sp
                CategoryItemSize.Large -> 12.sp
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected && size == CategoryItemSize.Small)
                SlashColors.Primary else SlashColors.SecondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Common StoreCard component for horizontal display (like in HomeScreen)
 */
@Composable
fun CommonStoreCardHorizontal(
    store: StoreListItem,
    calculateDistance: (String?, String?) -> String,
    onStoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .then(
                if (onStoreClick != null) {
                    Modifier.clickable { onStoreClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlashColors.StoreCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Store Name
            Text(
                text = store.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Store Image
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = SlashColors.StoreImageBackground
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AsyncImage(
                        model = store.logo ?: store.banner_image,
                        contentDescription = store.name,
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.store_default),
                        error = painterResource(id = R.drawable.store_default),
                        fallback = painterResource(id = R.drawable.store_default),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Distance section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SlashColors.StoreCardWhiteSection
            ) {
                Text(
                    text = "Distance - ${calculateDistance(store.latitude, store.longitude)}",
                    fontSize = 12.sp,
                    color = SlashColors.DistanceText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Store details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Category - ${store.category_name}",
                    fontSize = 12.sp,
                    color = SlashColors.CategoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Minimum Order - ₹${store.minimum_order_amount}",
                    fontSize = 12.sp,
                    color = SlashColors.CategoryText
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIP Disc - ",
                        fontSize = 12.sp,
                        color = SlashColors.VipDiscountRed
                    )
                    Text(
                        text = "${store.vip_discount_percentage}%",
                        fontSize = 12.sp,
                        color = SlashColors.DiscountRed,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discount - ",
                        fontSize = 12.sp,
                        color = SlashColors.CategoryText
                    )
                    Text(
                        text = "${store.normal_discount_percentage}%",
                        fontSize = 12.sp,
                        color = SlashColors.CategoryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Common StoreCard component for vertical display (like in StoreListScreen)
 */
@Composable
fun CommonStoreCardVertical(
    store: StoreListItem,
    calculateDistance: (String?, String?) -> String,
    onStoreClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .then(
                if (onStoreClick != null) {
                    Modifier.clickable { onStoreClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Store Image
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(8.dp),
                color = SlashColors.StoreImageBackground
            ) {
                AsyncImage(
                    model = store.logo ?: store.banner_image,
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.store_default),
                    error = painterResource(id = R.drawable.store_default),
                    fallback = painterResource(id = R.drawable.store_default),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Store Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Store Name
                Text(
                    text = store.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlashColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Store Info
                StoreInfoRow(
                    label = "Category",
                    value = store.category_name
                )
                StoreInfoRow(
                    label = "Minimum Order",
                    value = "₹${store.minimum_order_amount}"
                )
                StoreInfoRow(
                    label = "Distance",
                    value = calculateDistance(store.latitude, store.longitude)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // VIP Discount Badge
                if (store.vip_discount_percentage.toDouble() > 0) {
                    VipDiscountBadge(
                        discountPercentage = store.vip_discount_percentage
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$label - $value",
        fontSize = 12.sp,
        color = SlashColors.SecondaryText,
        modifier = modifier
    )
}

@Composable
private fun VipDiscountBadge(
    discountPercentage: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SlashColors.Primary,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.vip),
                contentDescription = "VIP",
                modifier = Modifier.size(12.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VIP Discount of $discountPercentage%",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Enum for different category item sizes
 */
enum class CategoryItemSize {
    Small,  // For store list screen (circular, smaller)
    Large   // For home screen (rectangular, larger)
}