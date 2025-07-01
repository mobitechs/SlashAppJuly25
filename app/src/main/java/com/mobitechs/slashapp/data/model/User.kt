package com.mobitechs.slashapp.data.model



data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isProfileComplete: Boolean = false,
    val isEmailVerified: Boolean = false,
    val referralCode: String = "",
    val profilePicture: String = ""
)



sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: User?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class ApiResponse(
    val status_code: Int,
    val message: String,
    val data: Any? = null
)