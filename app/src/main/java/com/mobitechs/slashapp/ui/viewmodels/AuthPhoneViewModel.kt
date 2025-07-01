package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// UI State for phone input screen
data class AuthPhoneUiState(
    val phoneNumber: String = "",
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

                if (response.status_code == 200) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToOtp = true
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

//class PhoneInputViewModel : ViewModel() {
//
//    private val authRepository = AuthRepository()
//
//    private val _uiState = MutableStateFlow(PhoneInputUiState())
//    val uiState: StateFlow<PhoneInputUiState> = _uiState.asStateFlow()
//
//    fun onPhoneNumberChange(phoneNumber: String) {
//        val filtered = phoneNumber.filter { it.isDigit() }
//        if (filtered.length <= 10) {
//            val isValid = PhoneValidator.isValidPhone(filtered)
//
//            _uiState.value = _uiState.value.copy(
//                phoneNumber = filtered,
//                phoneError = "", // Clear error on input change
//                isValidPhone = isValid,
//                hasError = false // Reset error state
//            )
//        }
//    }
//
//    fun sendOtp() {
//        val currentState = _uiState.value
//
//        if (!PhoneValidator.isValidPhone(currentState.phoneNumber)) {
//            _uiState.value = currentState.copy(
//                phoneError = "Please enter a valid Phone Number.",
//                hasError = true // Set error state
//            )
//            return
//        }
//
//        viewModelScope.launch {
//            authRepository.sendOtp("+91${currentState.phoneNumber}")
//                .collect { result ->
//                    when (result) {
//                        is AuthResult.Loading -> {
//                            _uiState.value = currentState.copy(isLoading = true)
//                        }
//                        is AuthResult.Success -> {
//                            _uiState.value = currentState.copy(
//                                isLoading = false,
//                                navigateToOtp = true
//                            )
//                        }
//                        is AuthResult.Error -> {
//                            _uiState.value = currentState.copy(
//                                isLoading = false,
//                                phoneError = result.message,
//                                hasError = true
//                            )
//                        }
//                    }
//                }
//        }
//    }
//
//    fun onNavigateToOtp() {
//        _uiState.value = _uiState.value.copy(navigateToOtp = false)
//    }
//}
//
//data class PhoneInputUiState(
//    val phoneNumber: String = "",
//    val phoneError: String = "",
//    val isValidPhone: Boolean = false,
//    val hasError: Boolean = false, // Added to track error state explicitly
//    val isLoading: Boolean = false,
//    val navigateToOtp: Boolean = false
//)