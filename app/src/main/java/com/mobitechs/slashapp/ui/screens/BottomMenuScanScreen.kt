package com.mobitechs.slashapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.mobitechs.slashapp.utils.CameraManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.navigation.NavController
import com.mobitechs.slashapp.Screen
import com.mobitechs.slashapp.ui.theme.SlashColors
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuScanViewModel
import com.mobitechs.slashapp.ui.viewmodels.QRScannerUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomMenuScanScreen(
    viewModel: BottomMenuScanViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1000)
        viewModel.simulateQRDetection("1") // This will auto-trigger with store ID 123
    }


    // Handle navigation to transaction screen
    LaunchedEffect(uiState.navigateToTransaction) {
        if (uiState.navigateToTransaction && uiState.storeDetails != null) {
            var storeId = uiState.storeDetails!!.id
            navController.navigate(Screen.TransactionScreen.route+"/$storeId")
            viewModel.onNavigateToTransaction()
        }
    }


    // Request camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Check initial permission state
    LaunchedEffect(Unit) {
        viewModel.checkCameraPermission(context)
    }



    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview Background
        if (uiState.hasPermission) {

            CameraPreview(
                onQRCodeDetected = { qrData ->
                    viewModel.processQRCode(qrData)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "📷",
                        fontSize = 64.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Camera Access Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To scan QR codes, we need access to your camera",
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Request permission when button is clicked
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Grant Camera Permission",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // QR Scanner Overlay (only show when permission is granted)
        if (uiState.hasPermission) {
            QRScannerOverlay(
                viewModel= viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onQRCodeScanned = { qrData ->
                    viewModel.processQRCode(qrData)
                },
                uiState = uiState
            )
        } else {
            // Simple back button overlay for permission screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
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
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SlashColors.Primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Processing QR Code...",
                            fontSize = 14.sp,
                            color = SlashColors.TextPrimary
                        )
                    }
                }
            }
        }

        // Error handling
        if (uiState.error.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌",
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Error",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlashColors.TextError
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error,
                            fontSize = 14.sp,
                            color = SlashColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.error.contains("permission", ignoreCase = true)) {
                                Button(
                                    onClick = {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary)
                                ) {
                                    Text("Grant Permission")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.clearError()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlashColors.Primary)
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Real Camera Preview with QR Code Detection
@Composable
fun CameraPreview(
    onQRCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager() }
    var isFlashOn by remember { mutableStateOf(false) }

    // Prevent multiple detections of the same QR code
    var lastDetectedQR by remember { mutableStateOf("") }
    var lastDetectionTime by remember { mutableStateOf(0L) }

    val throttledQRDetection = { qrData: String ->
        val currentTime = System.currentTimeMillis()
        if (qrData != lastDetectedQR || currentTime - lastDetectionTime > 2000) { // 2 second throttle
            lastDetectedQR = qrData
            lastDetectionTime = currentTime
            onQRCodeDetected(qrData)
        }
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            cameraManager.startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onQRCodeDetected = throttledQRDetection
            )
        }
    )

    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }
}

// QR Scanner Overlay - same as before but cleaned up
@Composable
fun QRScannerOverlay(
    viewModel: BottomMenuScanViewModel,
    onBackClick: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    uiState: QRScannerUiState
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

        Spacer(modifier = Modifier.weight(1f))

        // QR Scanner Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(
                        2.dp,
                        Color.White,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        // For testing - simulate QR scan with valid store ID
                        onQRCodeScanned("123")
                    }
            ) {
                QRCornerIndicators()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instructions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scan QR Code",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Position the QR code inside the frame to scan",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "QR code will be automatically detected when you position it between the guide lines",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Flash toggle button
            IconButton(
                onClick = {
                    viewModel.toggleFlash()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (uiState.isFlashOn) SlashColors.Primary.copy(alpha = 0.8f)
                        else Color.White.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = if (uiState.isFlashOn) Color.White else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// QR Corner Indicators - same as before
@Composable
fun QRCornerIndicators() {
    val cornerSize = 24.dp
    val strokeWidth = 4.dp
    val cornerColor = SlashColors.Primary

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left corner
        Canvas(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(8.dp, 8.dp)
                .size(cornerSize)
        ) {
            drawLine(
                color = cornerColor,
                start = Offset(0f, cornerSize.toPx() * 0.7f),
                end = Offset(0f, 0f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(0f, 0f),
                end = Offset(cornerSize.toPx() * 0.7f, 0f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Top-right corner
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((-8).dp, 8.dp)
                .size(cornerSize)
        ) {
            drawLine(
                color = cornerColor,
                start = Offset(cornerSize.toPx(), cornerSize.toPx() * 0.7f),
                end = Offset(cornerSize.toPx(), 0f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(cornerSize.toPx(), 0f),
                end = Offset(cornerSize.toPx() * 0.3f, 0f),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Bottom-left corner
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(8.dp, (-8).dp)
                .size(cornerSize)
        ) {
            drawLine(
                color = cornerColor,
                start = Offset(0f, cornerSize.toPx() * 0.3f),
                end = Offset(0f, cornerSize.toPx()),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(0f, cornerSize.toPx()),
                end = Offset(cornerSize.toPx() * 0.7f, cornerSize.toPx()),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Bottom-right corner
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset((-8).dp, (-8).dp)
                .size(cornerSize)
        ) {
            drawLine(
                color = cornerColor,
                start = Offset(cornerSize.toPx(), cornerSize.toPx() * 0.3f),
                end = Offset(cornerSize.toPx(), cornerSize.toPx()),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = cornerColor,
                start = Offset(cornerSize.toPx(), cornerSize.toPx()),
                end = Offset(cornerSize.toPx() * 0.3f, cornerSize.toPx()),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}