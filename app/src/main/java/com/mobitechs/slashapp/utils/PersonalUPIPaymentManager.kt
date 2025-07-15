// PersonalUPIPaymentManager.kt
package com.mobitechs.slashapp.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import com.mobitechs.slashapp.data.model.UPIPaymentRequest
import com.mobitechs.slashapp.data.model.UPIPaymentResult
import java.text.DecimalFormat

class PersonalUPIPaymentManager(private val activity: Activity?) {

    companion object {
        private const val TAG = "PersonalUPIManager"

        // Simplified UPI scheme for personal payments
        private const val UPI_SCHEME = "upi://pay"
    }

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
        }.distinctBy { it.packageName }
    }

    fun launchUPIPayment(
        activity: Activity,
        paymentRequest: UPIPaymentRequest,
        launcher: ActivityResultLauncher<Intent>
    ) {
        try {
            // Create simplified UPI URI for personal transfers
            val upiUri = buildSimpleUPIUri(paymentRequest)
            val intent = Intent(Intent.ACTION_VIEW, upiUri)

            // Set specific app package if provided
            paymentRequest.selectedAppPackage?.let { packageName ->
                intent.setPackage(packageName)
            }

            // Check if apps are available
            val packageManager = activity.packageManager
            val resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)


            if (resolveInfoList.isNotEmpty()) {
                launcher.launch(intent)
            } else {
                // Try without specific package
                val genericIntent = Intent(Intent.ACTION_VIEW, upiUri)
                val genericResolveList = packageManager.queryIntentActivities(genericIntent, PackageManager.MATCH_DEFAULT_ONLY)

                if (genericResolveList.isNotEmpty()) {
                    launcher.launch(genericIntent)
                } else {
                    throw Exception("No UPI apps found on device")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "UPI launch error", e)
            throw e
        }
    }

    /**
     * Build simplified UPI URI for personal transfers
     * Removes merchant-specific parameters that cause rejections
     */
    private fun buildSimpleUPIUri(request: UPIPaymentRequest): Uri {
        // Format amount properly
        val formattedAmount = formatAmount(request.amount)


        // Build minimal UPI URI - only essential parameters
        val uriBuilder = Uri.parse(UPI_SCHEME).buildUpon()
            .appendQueryParameter("pa", request.payeeVPA.trim())
            .appendQueryParameter("pn", request.payeeName.trim())
            .appendQueryParameter("tid", request.transactionId.trim())
            .appendQueryParameter("tr", request.transactionId.trim())
            .appendQueryParameter("tn", request.note)
            .appendQueryParameter("am", formattedAmount)
            .appendQueryParameter("cu", "INR")

        // DON'T add these parameters for personal UPI IDs:
        // - tid (transaction ID) - causes merchant validation
        // - tr (transaction reference) - merchant parameter
        // - mc (merchant code) - obviously merchant-specific
        // - Complex notes with business terminology

        return uriBuilder.build()
    }

    private fun formatAmount(amount: String): String {
        return try {
            val doubleAmount = amount.toDouble()
            if (doubleAmount <= 0) throw Exception("Invalid amount")

            // Format without trailing zeros
            val formatter = DecimalFormat("#0.##")
            formatter.format(doubleAmount)
        } catch (e: Exception) {
            throw Exception("Invalid amount format: $amount")
        }
    }

    fun parseUPIResponse(data: Intent?): UPIPaymentResult {
        if (data == null) {
            return UPIPaymentResult(
                isSuccess = false,
                errorMessage = "Payment cancelled by user"
            )
        }

        val response = data.getStringExtra("response") ?: ""

        if (response.isEmpty()) {
            // Try alternative response keys
            val responseAlternative = data.getStringExtra("UPI_Response")
                ?: data.getStringExtra("upi_response")
                ?: data.getStringExtra("paymentResponse")
                ?: ""

            return if (responseAlternative.isNotEmpty()) {
                parseUPIResponseString(responseAlternative)
            } else {
                UPIPaymentResult(
                    isSuccess = false,
                    errorMessage = "No response received from UPI app"
                )
            }
        }

        return parseUPIResponseString(response)
    }

    private fun parseUPIResponseString(response: String): UPIPaymentResult {
        val responseMap = mutableMapOf<String, String>()

        try {
            android.util.Log.d(TAG, "Parsing UPI response: $response")

            // Parse response parameters
            response.split("&").forEach { param ->
                val keyValue = param.split("=", limit = 2)
                if (keyValue.size == 2) {
                    responseMap[keyValue[0].trim()] = Uri.decode(keyValue[1].trim())
                }
            }

            android.util.Log.d(TAG, "Parsed response: $responseMap")

            val status = responseMap["Status"] ?: responseMap["status"] ?: ""
            val responseCode = responseMap["responseCode"] ?: responseMap["ResponseCode"] ?: ""
            val transactionId = responseMap["txnId"] ?: responseMap["TransactionId"] ?: ""
            val txnRef = responseMap["txnRef"] ?: responseMap["TransactionRef"] ?: ""

            val isSuccess = when {
                status.equals("SUCCESS", ignoreCase = true) -> true
                status.equals("SUBMITTED", ignoreCase = true) -> true
                responseCode == "0" -> true
                responseCode.equals("SUCCESS", ignoreCase = true) -> true
                else -> false
            }

            val errorMessage = if (!isSuccess) {
                getErrorMessage(responseCode, responseMap)
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
                errorMessage = "Failed to parse payment response"
            )
        }
    }

    private fun getErrorMessage(responseCode: String, responseMap: Map<String, String>): String {
        return responseMap["message"]
            ?: responseMap["errorMessage"]
            ?: responseMap["failureReason"]
            ?: when (responseCode) {
                "U16" -> "Payment cancelled by user"
                "U30" -> "Payment declined by bank"
                "U48" -> "Transaction limit exceeded for the day"
                "U49" -> "Invalid UPI address"
                "U52" -> "Insufficient funds in account"
                "U69" -> "Transaction not permitted"
                "ZA" -> "Invalid payment request format"
                "ZM" -> "Invalid UPI address format"
                else -> "Payment failed. Please try again."
            }
    }
}

