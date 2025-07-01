package com.mobitechs.slashapp.utils

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import java.util.regex.Pattern



object PhoneValidator {
    fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }

    fun formatPhoneNumber(phone: String): String {
        return "+91 $phone"
    }
}

object OtpValidator {
    fun isValidOtp(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }
}

fun showToast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}