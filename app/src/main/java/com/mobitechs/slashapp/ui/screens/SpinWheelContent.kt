import android.graphics.Color.parseColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobitechs.slashapp.data.model.WheelSegmentItems
import com.mobitechs.slashapp.ui.viewmodels.DailyRewardsViewModel
import kotlin.math.*

@Composable
fun SpinWheelContent(
    viewModel: DailyRewardsViewModel,
    showHeader: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    // Animation state for wheel rotation
    var isAnimating by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isAnimating) 1800f else 0f,
        animationSpec = tween(
            durationMillis = 3000,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            isAnimating = false
        }
    )

    // Start animation when spinning
    LaunchedEffect(uiState.isSpinning) {
        if (uiState.isSpinning) {
            isAnimating = true
        }
    }

    // Show result dialog
    if (uiState.showResultDialog && uiState.spinResult != null) {
        SpinResultDialog(
            result = uiState.spinResult!!,
            onDismiss = { viewModel.dismissResultDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7B1FA2), // Purple like the image background
                        Color(0xFF4A148C),
                        Color(0xFF6A1B9A),
                        Color.White
                    )
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        if (!showHeader) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Campaign info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.currentCampaign?.description ?: "",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    SpinInfoItem(
                        label = "Spins Left",
                        value = uiState.remainingSpins.toString(),
                        icon = Icons.Default.Refresh
                    )
                    SpinInfoItem(
                        label = "Today's Spins",
                        value = uiState.todaySpins.toString(),
                        icon = Icons.Default.Today
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Beautiful Spin Wheel using exact API data
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentAlignment = Alignment.Center
        ) {
            BeautifulSpinWheel(
                segments = uiState.wheelSegments,
                rotationAngle = rotationAngle,
                modifier = Modifier.size(320.dp)
            )

            // Center pointer (pointing down)
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Pointer",
                tint = Color(0xFFB8860B),
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = (-160).dp)
                    .rotate(90f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Spin Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.spinWheel() },
                enabled = uiState.canSpin && !uiState.isSpinning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSpinning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Spinning...", fontSize = 16.sp, color = Color.White)
                } else {
                    Text(
                        text = "SPIN NOW!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (!uiState.canSpin && uiState.remainingSpins == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Come back tomorrow for more spins!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Prize preview using exact API data
//        if (uiState.wheelSegments.isNotEmpty()) {
//            PrizePreviewSection(segments = uiState.wheelSegments)
//        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BeautifulSpinWheel(
    segments: List<WheelSegmentItems>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Wheel background with gradient
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700), // Gold center
                            Color(0xFFB8860B)  // Darker gold edge
                        ),
                        radius = 150f
                    ),
                    shape = CircleShape
                )
                .padding(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
            ) {
                if (segments.isEmpty()) return@Canvas

                val radius = size.minDimension / 2
                val center = Offset(size.width / 2, size.height / 2)
                val anglePerSegment = 360f / segments.size

                segments.forEachIndexed { index, segment ->
                    val startAngle = index * anglePerSegment
                    val sweepAngle = anglePerSegment

                    // Use exact color from API response
                    val segmentColor = try {
                        Color(parseColor(segment.color))
                    } catch (e: Exception) {
                        Color.Gray // Fallback only if color parsing fails
                    }

                    // Draw segment with gradient using API color
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                segmentColor,
                                segmentColor.copy(alpha = 0.8f)
                            ),
                            radius = radius * 0.8f
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Draw segment border
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Draw outer decorative border
                drawCircle(
                    color = Color(0xFFB8860B),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )

                // Draw ONE white dot per segment - positioned INSIDE the border
                segments.forEachIndexed { index, _ ->
                    val startAngle = index * anglePerSegment
                    val sweepAngle = anglePerSegment
                    val dotAngle = (startAngle + sweepAngle / 2) * (PI / 180f)

                    // Position dots well inside the border to avoid being cut
                    val dotRadius = radius - 12.dp.toPx() // Move dots inside the border
                    val dotX = center.x + cos(dotAngle.toFloat()) * dotRadius
                    val dotY = center.y + sin(dotAngle.toFloat()) * dotRadius

                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(), // Make dots slightly bigger
                        center = Offset(dotX, dotY)
                    )
                }

                // Draw center circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFB8860B)
                        )
                    ),
                    radius = radius * 0.15f,
                    center = center
                )

                // Draw center pin
                drawCircle(
                    color = Color(0xFF8B4513), // Brown
                    radius = radius * 0.08f,
                    center = center
                )
            }
        }

        // Overlay text on segments using exact API text
        segments.forEachIndexed { index, segment ->
            val anglePerSegment = 360f / segments.size
            val segmentAngle = index * anglePerSegment + anglePerSegment / 2

            TrulyVerticalSegmentText(
                segment = segment,
                angle = segmentAngle,
                rotationAngle = rotationAngle,
                wheelRadius = 150.dp
            )
        }
    }
}

@Composable
private fun TrulyVerticalSegmentText(
    segment: WheelSegmentItems,
    angle: Float,
    rotationAngle: Float,
    wheelRadius: Dp
) {
    val totalAngle = angle + rotationAngle
    val radians = Math.toRadians(totalAngle.toDouble())

    // Use exact text from API without any modifications
    val displayText = segment.text

    // Larger font sizes for better readability
    val fontSize = when {
        displayText.length > 15 -> 10.sp
        displayText.length > 10 -> 11.sp
        displayText.length > 8 -> 12.sp
        else -> 13.sp
    }

    // Position text at 60% radius for all text
    val textRadius = wheelRadius * 0.6f
    val textX = (cos(radians) * textRadius.value).dp
    val textY = (sin(radians) * textRadius.value).dp

    Box(
        modifier = Modifier
            .offset(x = textX, y = textY)
            .rotate(totalAngle) // Text reads from center outward
            .width(50.dp), // Give more width for text wrapping
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText, // Use exact API text
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = fontSize * 1.2f,
            maxLines = 2, // Allow 2 lines for better readability
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.9f),
                    offset = Offset(1f, 1f),
                    blurRadius = 3f
                )
            )
        )
    }
}


@Composable
private fun SpinInfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrizePreviewSection(
    segments: List<WheelSegmentItems>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Possible Rewards",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            segments.take(6).forEach { segment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = try {
                                        Color(android.graphics.Color.parseColor(segment.color))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = segment.text,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (segment.reward_type == "cashback" && segment.reward_value > 0) {
                        Text(
                            text = "₹${segment.reward_value}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpinResultDialog(
    result: com.mobitechs.slashapp.data.model.RewardData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (result.is_winner) "🎉 Congratulations!" else "Better Luck Next Time!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.is_winner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (result.is_winner) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = try {
                                Color(android.graphics.Color.parseColor(result.display_color))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = result.display_text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = try {
                                    Color(android.graphics.Color.parseColor(result.display_color))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                            if (result.type == "cashback") {
                                Text(
                                    text = "Added to your wallet!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Don't worry, you can try again tomorrow!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Awesome!", color = Color.White)
            }
        }
    )
}