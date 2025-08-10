package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for phone input screen
data class AuthPhoneUiState(
    val phoneNumber: String = "8655883061",
    val otp: String = "123456",
    val otpExpiry: String = "",
    val phoneError: String = "",
    val isValidPhone: Boolean = false,
    val hasError: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = "",
    val navigateToOtp: Boolean = false
)

class AuthPhoneViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthPhoneUiState())
    val uiState: StateFlow<AuthPhoneUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(phoneNumber: String) {
        _uiState.update {
            it.copy(
                phoneNumber = phoneNumber,
                phoneError = validatePhoneNumber(phoneNumber),
                isValidPhone = isValidPhoneNumber(phoneNumber),
                hasError = false,
                error = "" // Clear any previous errors
            )
        }
    }

    fun sendOtp() {
        val phoneNumber = uiState.value.phoneNumber
        val phoneError = validatePhoneNumber(phoneNumber)

        if (phoneError.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    phoneError = phoneError,
                    hasError = true
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val response = authRepository.sendOtp(phoneNumber)

                if (response.success == true) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToOtp = true,
                            otp = response.data.otp,
                            otpExpiry = response.data.expires_in

                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message,
                            hasError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred",
                        hasError = true
                    )
                }
            }
        }
    }

    fun onNavigateToOtp() {
        _uiState.update { it.copy(navigateToOtp = false) }
    }

    private fun validatePhoneNumber(phone: String): String {
        return when {
            phone.isEmpty() -> "Phone number is required"
            phone.length < 10 -> "Phone number must be at least 10 digits"
            !phone.all { it.isDigit() } -> "Phone number should contain only digits"
            else -> ""
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length >= 10 && phone.all { it.isDigit() }
    }
}
