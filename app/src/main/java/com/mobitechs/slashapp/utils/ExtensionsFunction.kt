package com.mobitechs.slashapp.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast
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

//
//fun validatePhoneNumber(phone: String): String {
//    return when {
//        phone.isEmpty() -> "Phone number is required"
//        phone.length < 10 -> "Phone number must be at least 10 digits"
//        !phone.all { it.isDigit() } -> "Phone number should contain only digits"
//        else -> ""
//    }
//}
//
//fun isValidPhoneNumber(phone: String): Boolean {
//    return phone.length >= 10 && phone.all { it.isDigit() }
//}
//
//fun validateEmail(email: String): String {
//    return when {
//        email.isEmpty() -> "Email is required"
//        !email.contains("@") -> "Email must contain '@' symbol"
//        !email.contains(".") -> "Email must contain a dot '.' symbol"
//        email.indexOf('@') > email.indexOf('.') -> "The dot (.) should come after the '@' symbol"
//        else -> ""
//    }
//}
//
//fun isValidEmail(email: String): Boolean {
//    return email.contains("@") && email.contains(".") && email.indexOf('@') < email.indexOf('.')
//}
//
//fun validateInputFiled(input: String, inputFiledName:String ): String {
//    return when {
//        input.isEmpty() -> "$inputFiledName is required"
//        else -> ""
//    }
//}
//
//fun isValidInputField(input: String): Boolean {
//    return input.isEmpty()
//}