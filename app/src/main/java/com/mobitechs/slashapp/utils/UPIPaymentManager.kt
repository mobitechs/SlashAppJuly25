// UPIPaymentManager.kt
package com.mobitechs.slashapp.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.mobitechs.slashapp.data.model.UPIPaymentRequest
import com.mobitechs.slashapp.data.model.UPIPaymentResult
import java.text.DecimalFormat
import java.util.UUID

class UPIPaymentManager2(private val activity: Activity?) {

    companion object {
        private const val UPI_SCHEME = "upi://pay"
        private const val TAG = "UPIPaymentManager"
    }

    // Get list of UPI apps installed on device
    fun getUPIApps(): List<UPIApp> {
        if (activity == null) return emptyList()

        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_SCHEME))
        val packageManager = activity.packageManager
        val resolveInfoList = packageManager.queryIntentActivities(upiIntent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfoList.map { resolveInfo ->
            UPIApp(
                packageName = resolveInfo.activityInfo.packageName,
                appName = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager)
            )
        }.distinctBy { it.packageName } // Remove duplicates
    }

    fun launchUPIPayment(
        activity: Activity,
        paymentRequest: UPIPaymentRequest,
        launcher: ActivityResultLauncher<Intent>
    ) {
        try {
            // Validate inputs
            validatePaymentRequest(paymentRequest)

            val upiUri = buildUPIUri(paymentRequest)
            android.util.Log.d(TAG, "UPI URI: $upiUri")

            val intent = Intent(Intent.ACTION_VIEW, upiUri)

            // If specific app package is provided, set it
            paymentRequest.selectedAppPackage?.let { packageName ->
                intent.setPackage(packageName)
                android.util.Log.d(TAG, "Setting package: $packageName")
            }

            // Check if any UPI apps are available
            val packageManager = activity.packageManager
            val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            android.util.Log.d(TAG, "Found ${resolveInfoList.size} UPI apps for intent")

            if (resolveInfoList.isNotEmpty()) {
                launcher.launch(intent)
            } else {
                // Try without specific package if no apps found
                if (paymentRequest.selectedAppPackage != null) {
                    val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                    val genericResolveList = packageManager.queryIntentActivities(genericIntent, PackageManager.MATCH_DEFAULT_ONLY)

                    if (genericResolveList.isNotEmpty()) {
                        launcher.launch(genericIntent)
                    } else {
                        throw Exception("No UPI apps found on device")
                    }
                } else {
                    throw Exception("No UPI apps found on device")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "UPI launch error", e)
            throw e
        }
    }

    private fun validatePaymentRequest(request: UPIPaymentRequest) {
        // Validate UPI VPA format
        if (!isValidUPIAddress(request.payeeVPA)) {
            throw Exception("Invalid UPI address format: ${request.payeeVPA}")
        }

        // Validate amount
        val amount = request.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            throw Exception("Invalid amount: ${request.amount}")
        }

        if (amount < 1.0) {
            throw Exception("Amount must be at least ₹1.00")
        }

        if (amount > 200000.0) { // UPI limit
            throw Exception("Amount exceeds UPI limit of ₹2,00,000")
        }

        // Validate payee name
        if (request.payeeName.isBlank()) {
            throw Exception("Payee name cannot be empty")
        }

        if (request.payeeName.length > 99) {
            throw Exception("Payee name too long (max 99 characters)")
        }
    }

    private fun isValidUPIAddress(vpa: String): Boolean {
        if (vpa.isBlank()) return false

        // UPI address pattern: username@bank
        val upiPattern = "^[a-zA-Z0-9][a-zA-Z0-9._-]{2,49}@[a-zA-Z0-9][a-zA-Z0-9.-]{2,49}$".toRegex()

        return vpa.matches(upiPattern) &&
                vpa.contains("@") &&
                !vpa.startsWith("@") &&
                !vpa.endsWith("@") &&
                vpa.length >= 6 &&
                vpa.length <= 99
    }

    // Build UPI URI for payment with proper formatting
    private fun buildUPIUri(request: UPIPaymentRequest): Uri {
        // Generate proper transaction ID if not provided
        val transactionId = if (request.transactionId.isNotEmpty()) {
            request.transactionId
        } else {
            generateTransactionId()
        }

        // Format amount properly (no trailing zeros, max 2 decimal places)
        val formattedAmount = formatAmount(request.amount)

        // Build URI with all required parameters
        val uriBuilder = Uri.parse(UPI_SCHEME).buildUpon()
            .appendQueryParameter("pa", request.payeeVPA.trim())
            .appendQueryParameter("pn", sanitizePayeeName(request.payeeName))
            .appendQueryParameter("am", formattedAmount)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tid", transactionId)

        // Add transaction note if provided
        if (request.note.isNotEmpty()) {
            val sanitizedNote = sanitizeTransactionNote(request.note)
            uriBuilder.appendQueryParameter("tn", sanitizedNote)
        }

        // Add transaction reference if provided
        if (request.transactionRef.isNotEmpty()) {
            uriBuilder.appendQueryParameter("tr", request.transactionRef.trim())
        }

        // Add merchant category code for better acceptance
        uriBuilder.appendQueryParameter("mc", "5411") // Grocery stores, supermarkets

        return uriBuilder.build()
    }

    private fun generateTransactionId(): String {
        val timestamp = System.currentTimeMillis()
        val random = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        return "SLA${timestamp}${random}".take(35) // UPI transaction ID limit
    }

    private fun formatAmount(amount: String): String {
        return try {
            val doubleAmount = amount.toDouble()
            val formatter = DecimalFormat("#0.##")
            formatter.format(doubleAmount)
        } catch (e: Exception) {
            amount
        }
    }

    private fun sanitizePayeeName(name: String): String {
        // Remove special characters and limit length
        return name.trim()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .take(50)
            .trim()
    }

    private fun sanitizeTransactionNote(note: String): String {
        // Remove special characters that might cause issues
        return note.trim()
            .replace(Regex("[^a-zA-Z0-9\\s.,()-]"), "")
            .take(50)
            .trim()
    }

    // Enhanced UPI response parsing
    fun parseUPIResponse(data: Intent?): UPIPaymentResult {
        if (data == null) {
            return UPIPaymentResult(
                isSuccess = false,
                errorMessage = "Payment cancelled by user"
            )
        }

        val response = data.getStringExtra("response")

        if (response.isNullOrEmpty()) {
            // Try alternative response keys
            val responseAlternative = data.getStringExtra("UPI_Response")
                ?: data.getStringExtra("upi_response")
                ?: data.getStringExtra("paymentResponse")

            if (responseAlternative.isNullOrEmpty()) {
                // Check if payment was successful based on result code
                return UPIPaymentResult(
                    isSuccess = false,
                    errorMessage = "No response received from UPI app"
                )
            } else {
                return parseUPIResponseString(responseAlternative)
            }
        }

        return parseUPIResponseString(response)
    }

    private fun parseUPIResponseString(response: String): UPIPaymentResult {
        val responseMap = mutableMapOf<String, String>()

        try {
            android.util.Log.d(TAG, "Parsing UPI response: $response")

            // Parse the response string (format: key1=value1&key2=value2)
            response.split("&").forEach { param ->
                val keyValue = param.split("=", limit = 2)
                if (keyValue.size == 2) {
                    responseMap[keyValue[0].trim()] = Uri.decode(keyValue[1].trim())
                }
            }

            android.util.Log.d(TAG, "Parsed response map: $responseMap")

            val status = responseMap["Status"] ?: responseMap["status"] ?: ""
            val responseCode = responseMap["responseCode"] ?: responseMap["ResponseCode"] ?: ""
            val transactionId = responseMap["txnId"] ?: responseMap["TransactionId"] ?: responseMap["transaction_id"] ?: ""
            val txnRef = responseMap["txnRef"] ?: responseMap["TransactionRef"] ?: responseMap["transaction_ref"] ?: ""
            val approvalRef = responseMap["ApprovalRefNo"] ?: responseMap["approval_ref"] ?: ""

            // More comprehensive success checking
            val isSuccess = when {
                status.equals("SUCCESS", ignoreCase = true) -> true
                status.equals("SUBMITTED", ignoreCase = true) -> true
                status.equals("PENDING", ignoreCase = true) -> true
                responseCode == "0" -> true
                responseCode.equals("SUCCESS", ignoreCase = true) -> true
                approvalRef.isNotEmpty() -> true
                else -> false
            }

            val errorMessage = if (!isSuccess) {
                responseMap["message"]
                    ?: responseMap["errorMessage"]
                    ?: responseMap["failureReason"]
                    ?: responseMap["error"]
                    ?: when (responseCode) {
                        "U30" -> "Payment declined by bank"
                        "U48" -> "Transaction limit exceeded"
                        "U49" -> "Invalid VPA"
                        "U50" -> "Account blocked"
                        "U51" -> "Invalid account"
                        "U52" -> "Insufficient funds"
                        "U69" -> "Transaction not permitted"
                        "ZA" -> "Invalid format"
                        "ZM" -> "Invalid VPA"
                        else -> "Payment failed (Code: $responseCode)"
                    }
            } else ""

            return UPIPaymentResult(
                isSuccess = isSuccess,
                transactionId = transactionId,
                responseCode = responseCode,
                status = status,
                txnRef = txnRef,
                errorMessage = errorMessage
            )

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error parsing UPI response", e)
            return UPIPaymentResult(
                isSuccess = false,
                errorMessage = "Failed to parse payment response: ${e.message}"
            )
        }
    }
}

// Data class for UPI app info
data class UPIApp(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable
)