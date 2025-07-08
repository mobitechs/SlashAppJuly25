package com.mobitechs.slashapp.ui.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobitechs.slashapp.data.model.StoreListItem
import com.mobitechs.slashapp.data.repository.QRScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// QR Scanner UI State
data class QRScannerUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val hasPermission: Boolean = false,
    val storeDetails: StoreListItem? = null,
    val navigateToTransaction: Boolean = false,
    val isFlashOn: Boolean = false
)

class BottomMenuScanViewModel(
    private val qrScannerRepository: QRScannerRepository? = null // Make optional for now
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(QRScannerUiState())
    val uiState: StateFlow<QRScannerUiState> = _uiState.asStateFlow()

    fun checkCameraPermission(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.update {
            it.copy(
                hasPermission = hasPermission,
                error = if (!hasPermission) "" else it.error // Clear error if permission granted
            )
        }
    }

    fun onPermissionGranted() {
        _uiState.update {
            it.copy(
                hasPermission = true,
                error = ""
            )
        }
    }

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                hasPermission = false,
                error = "Camera permission is required to scan QR codes"
            )
        }
    }

    fun processQRCode(qrData: String) {
        if (!_uiState.value.hasPermission) {
            _uiState.update { it.copy(error = "Camera permission is required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                // Extract store ID from QR data
                val storeId = extractStoreIdFromQR(qrData)

                if (storeId > 0) {
                    // If repository is available, use it. Otherwise, simulate response for testing
                    if (qrScannerRepository != null) {
                        val storeResponse = qrScannerRepository.getStoreDetails(storeId)

                        if (storeResponse.success && storeResponse.data != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    storeDetails = storeResponse.data,
                                    navigateToTransaction = true,
                                    error = ""
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Store not found or inactive"
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid QR Code. Please scan a valid store QR code."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to process QR code. Please try again."
                    )
                }
            }
        }
    }

    private fun extractStoreIdFromQR(qrData: String): Int {
        return try {
            when {
                // Handle different QR code formats
                qrData.startsWith("store_id:") -> {
                    qrData.substringAfter("store_id:").toIntOrNull() ?: 0
                }
                qrData.contains("storeId=") -> {
                    qrData.substringAfter("storeId=").substringBefore("&").toIntOrNull() ?: 0
                }
                qrData.startsWith("{") && qrData.endsWith("}") -> {
                    // JSON format
                    val gson = Gson()
                    val json = gson.fromJson(qrData, JsonObject::class.java)
                    json.get("store_id")?.asInt ?: json.get("storeId")?.asInt ?: 0
                }
                qrData.toIntOrNull() != null -> {
                    // Simple store ID
                    val id = qrData.toInt()
                    if (id > 0) id else 0
                }
                qrData.startsWith("https://") || qrData.startsWith("http://") -> {
                    // URL format - extract store ID from URL
                    extractStoreIdFromUrl(qrData)
                }
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun extractStoreIdFromUrl(url: String): Int {
        return try {
            when {
                url.contains("store_id=") -> {
                    url.substringAfter("store_id=").substringBefore("&").toIntOrNull() ?: 0
                }
                url.contains("storeId=") -> {
                    url.substringAfter("storeId=").substringBefore("&").toIntOrNull() ?: 0
                }
                url.contains("/store/") -> {
                    url.substringAfter("/store/").substringBefore("/").substringBefore("?").toIntOrNull() ?: 0
                }
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    fun onNavigateToTransaction() {
        _uiState.update { it.copy(navigateToTransaction = false) }
    }

    // Clear error message
    fun clearError() {
        _uiState.update { it.copy(error = "") }
    }

    // Toggle flash (this will be called from the UI)
    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun simulateQRDetection(storeId: String = "1") {
        processQRCode(storeId)
    }
}
