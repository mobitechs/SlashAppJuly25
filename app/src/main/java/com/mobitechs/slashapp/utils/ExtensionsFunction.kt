package com.mobitechs.slashapp.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import java.util.UUID
import java.util.regex.Pattern


fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

// Simple validation result
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
)

// Generic validation functions
object ValidationUtils {

    // Generic required field validation
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult(false, "$fieldName is required")
        } else {
            ValidationResult(true)
        }
    }

    // Email validation
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true)
        }
    }

    // Phone validation
    fun validatePhoneNumber(phone: String): ValidationResult {
        val cleanPhone = phone.replace(Regex("[^\\d]"), "")
        return when {
            cleanPhone.isEmpty() -> ValidationResult(false, "Phone number is required")
            cleanPhone.length != 10 -> ValidationResult(false, "Phone number must be exactly 10 digits")
            !cleanPhone.matches(Regex("^[6-9]\\d{9}$")) ->
                ValidationResult(false, "Please enter a valid phone number")
            else -> ValidationResult(true)
        }
    }

    // OTP validation
    fun validateOtp(otp: String): ValidationResult {
        return when {
            otp.isEmpty() -> ValidationResult(false, "OTP is required")
            otp.length != 6 -> ValidationResult(false, "OTP must be 6 digits")
            !otp.matches(Regex("^\\d{6}$")) -> ValidationResult(false, "OTP should contain only numbers")
            else -> ValidationResult(true)
        }
    }

    // Optional field validation (for referral code, etc.)
    fun validateOptional(value: String, fieldName: String, maxLength: Int = 50): ValidationResult {
        return if (value.isNotEmpty() && value.length > maxLength) {
            ValidationResult(false, "$fieldName must be less than $maxLength characters")
        } else {
            ValidationResult(true)
        }
    }
}

// Extension functions for convenience
fun String.isValidRequired(fieldName: String): ValidationResult =
    ValidationUtils.validateRequired(this, fieldName)

fun String.isValidEmail(): ValidationResult =
    ValidationUtils.validateEmail(this)

fun String.isValidPhone(): ValidationResult =
    ValidationUtils.validatePhoneNumber(this)

fun String.isValidOtp(): ValidationResult =
    ValidationUtils.validateOtp(this)

fun String.isValidOptional(fieldName: String, maxLength: Int = 50): ValidationResult =
    ValidationUtils.validateOptional(this, fieldName, maxLength)


fun generateTransactionId(): String {
    val transactionId = UUID.randomUUID().toString().take(8)
    return transactionId
}

fun formatDecimalString(value: String): String {
    if (value.isEmpty()) return value

    return try {
        val doubleValue = value.toDouble()
        if (doubleValue % 1.0 == 0.0) {
            // It's a whole number, remove decimal point
            doubleValue.toInt().toString()
        } else {
            // It has decimal places, keep them but remove trailing zeros
            value.toBigDecimal().stripTrailingZeros().toPlainString()
        }
    } catch (e: NumberFormatException) {
        value // Return original if not a valid number
    }
}

// Helper functions to format amounts and percentages
fun formatAmount(amount: String): String {
    return try {
        val doubleValue = amount.toDouble()
        if (doubleValue == doubleValue.toInt().toDouble()) {
            doubleValue.toInt().toString()
        } else {
            amount
        }
    } catch (e: NumberFormatException) {
        amount
    }
}

fun formatPercentage(percentage: String): String {
    return try {
        val doubleValue = percentage.toDouble()
        if (doubleValue == doubleValue.toInt().toDouble()) {
            doubleValue.toInt().toString()
        } else {
            percentage
        }
    } catch (e: NumberFormatException) {
        percentage
    }
}