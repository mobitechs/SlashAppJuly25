package com.mobitechs.slashapp.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mobitechs.slashapp.R
import com.mobitechs.slashapp.data.model.StoreReviewsListItem
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.AddReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    viewModel: AddReviewViewModel,
    navController: NavController,
    storeId: String,
    existingReview: StoreReviewsListItem? = null // null for add, populated for update
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize ViewModel
    LaunchedEffect(storeId, existingReview) {
        viewModel.initialize(storeId, existingReview)
    }

    // Navigate back on successful submission
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            navController.navigateUp()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Store Image with Back Button
            if (uiState.store != null) {
                StoreImageSection(
                    store = uiState.store!!,
                    onBackClick = { navController.navigateUp() }
                )

                // Store Info Section
                StoreInfoSection(
                    store = uiState.store!!,
                    isFavorite = uiState.isFavorite,
                    isUpdatingFavorite = uiState.isUpdatingFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.isStoreLoading) {
                // Loading state for store
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SlashColors.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else if (uiState.storeError.isNotEmpty()) {
                // Error state for store
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.storeError,
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.retryLoadStore() },
                            colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Retry", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Review Form Section
            ReviewFormSection(
                isUpdate = uiState.isUpdateMode,
                screenTitle = viewModel.getScreenTitle(),
                userName = viewModel.getUserName(),
                selectedRating = uiState.selectedRating,
                reviewTitle = uiState.reviewTitle,
                reviewDescription = uiState.reviewDescription,
                isSubmitting = uiState.isSubmitting,
                error = uiState.submissionError,
                submitButtonText = viewModel.getSubmitButtonText(),
                isFormValid = viewModel.isFormValid(),
                onRatingSelected = { viewModel.updateRating(it) },
                onTitleChanged = { viewModel.updateTitle(it) },
                onDescriptionChanged = { viewModel.updateDescription(it) },
                onSubmit = { viewModel.submitReview() },
                onCancel = { navController.navigateUp() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun StoreImageSection(
    store: com.mobitechs.slashapp.data.model.StoreListItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Store Image
        AsyncImage(
            model = store.banner_image ?: store.logo,
            contentDescription = store.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.store_default),
            error = painterResource(id = R.drawable.store_default)
        )

        // Back Button Overlay
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun StoreInfoSection(
    store: com.mobitechs.slashapp.data.model.StoreListItem,
    isFavorite: Boolean,
    isUpdatingFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Store Name
                Text(
                    text = store.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlashColors.PrimaryText
                )

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = store.rating,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlashColors.PrimaryText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    repeat(5) { index ->
                        val rating = store.rating.toFloatOrNull() ?: 0f
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Address
                if (!store.address.isNullOrEmpty()) {
                    Text(
                        text = store.address,
                        fontSize = 12.sp,
                        color = SlashColors.SecondaryText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                enabled = !isUpdatingFavorite
            ) {
                if (isUpdatingFavorite) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SlashColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else SlashColors.SecondaryText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewFormSection(
    isUpdate: Boolean,
    screenTitle: String,
    userName: String?,
    selectedRating: Int,
    reviewTitle: String,
    reviewDescription: String,
    isSubmitting: Boolean,
    error: String,
    submitButtonText: String,
    isFormValid: Boolean,
    onRatingSelected: (Int) -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Title
            Text(
                text = screenTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlashColors.PrimaryText
            )

            if (isUpdate && !userName.isNullOrEmpty()) {
                Text(
                    text = "($userName)",
                    fontSize = 14.sp,
                    color = SlashColors.SecondaryText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Score Section
            Text(
                text = "Score*",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SlashColors.PrimaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star ${index + 1}",
                        tint = if (index < selectedRating) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onRatingSelected(index + 1) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Input
            OutlinedTextField(
                value = reviewTitle,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "SO DELICIOUS 😋😎",
                        color = SlashColors.SecondaryText.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SlashColors.Primary,
                    unfocusedBorderColor = SlashColors.SecondaryText.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Input
            OutlinedTextField(
                value = reviewDescription,
                onValueChange = onDescriptionChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        text = "\"Lobortis leo pretium facilisis amet nisl at nec. Scelerisque risus tortor donec ipsum consequat semper consequat adipiscing ultrices.\"",
                        color = SlashColors.SecondaryText.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SlashColors.Primary,
                    unfocusedBorderColor = SlashColors.SecondaryText.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                )
            )

            // Error Message
            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SlashColors.SecondaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        SlashColors.SecondaryText.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Submit Button
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting && isFormValid
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = submitButtonText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}