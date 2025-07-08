package com.mobitechs.slashapp.utils

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onQRCodeDetected: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Image analyzer for QR code detection
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, QRCodeAnalyzer(onQRCodeDetected))
                    }

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider?.unbindAll()

                    // Bind use cases to camera
                    camera = cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )

                } catch (exc: Exception) {
                    // Handle binding failure
                }

            } catch (exc: Exception) {
                // Handle camera provider initialization failure
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun toggleFlash(): Boolean {
        return camera?.let { camera ->
            val currentFlashMode = camera.cameraInfo.torchState.value ?: TorchState.OFF
            val newFlashMode = currentFlashMode == TorchState.OFF
            camera.cameraControl.enableTorch(newFlashMode)
            newFlashMode
        } ?: false
    }

    fun isFlashOn(): Boolean {
        return camera?.cameraInfo?.torchState?.value == TorchState.ON
    }

    fun shutdown() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}