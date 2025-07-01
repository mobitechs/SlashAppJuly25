package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.User
import com.mobitechs.slashapp.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


// UI State for OTP verification screen
data class AuthOtpVerificationUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val otpError: String = "",
    val isValidOtp: Boolean = false,
    val hasError: Boolean = false,
    val isLoading: Boolean = false,
    val error: String = "",
    val user: User? = null,
    val navigateToNext: Boolean = false
)

class AuthOtpVerificationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthOtpVerificationUiState())
    val uiState: StateFlow<AuthOtpVerificationUiState> = _uiState.asStateFlow()

    private val _resendTimer = MutableStateFlow(45)
    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()

    init {
        startResendTimer()
    }

    fun setPhoneNumber(phoneNumber: String) {
        _uiState.update { it.copy(phoneNumber = phoneNumber) }
    }

    fun onOtpChange(otp: String) {
        val isValid = otp.length == 6

        _uiState.update {
            it.copy(
                otp = otp,
                otpError = "", // Clear error on input change
                isValidOtp = isValid,
                hasError = false, // Reset error state
                error = "" // Clear any previous errors
            )
        }

        // Auto submit when OTP is complete and valid
        if (otp.length == 6) {
            verifyOtp()
        }
    }

    fun verifyOtp() {
        val currentState = _uiState.value

        if (currentState.otp.length != 6) {
            _uiState.update {
                it.copy(
                    otpError = "Please enter complete OTP",
                    hasError = true
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val response = authRepository.verifyOtp(currentState.phoneNumber, currentState.otp)

                if (response.status_code == 200) {
                    // Parse user data from response if available
                    val user = response.data?.let {
                        // Convert response data to User object
                        // This depends on your API response structure
                        User(
                            id = "user_id", // Extract from response
                            phoneNumber = currentState.phoneNumber
                            // Add other fields as needed
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            navigateToNext = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpError = response.message,
                            hasError = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        otpError = e.message ?: "Unknown error occurred",
                        hasError = true
                    )
                }
            }
        }
    }

    fun resendOtp() {
        if (_resendTimer.value == 0) {
            viewModelScope.launch {
                try {
                    val response = authRepository.sendOtp(_uiState.value.phoneNumber)

                    if (response.status_code == 200) {
                        _resendTimer.value = 45
                        startResendTimer()
                        // Clear any existing OTP and errors
                        _uiState.update {
                            it.copy(
                                otp = "",
                                otpError = "",
                                hasError = false,
                                error = ""
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                error = response.message,
                                hasError = true
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to resend OTP",
                            hasError = true
                        )
                    }
                }
            }
        }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value = _resendTimer.value - 1
            }
        }
    }

    fun onNavigateToNext() {
        _uiState.update { it.copy(navigateToNext = false) }
    }
}

//class OtpVerificationViewModel : ViewModel() {
//
//    private val authRepository = AuthRepository()
//
//    private val _uiState = MutableStateFlow(OtpVerificationUiState())
//    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()
//
//    private val _resendTimer = MutableStateFlow(45)
//    val resendTimer: StateFlow<Int> = _resendTimer.asStateFlow()
//
//    init {
//        startResendTimer()
//    }
//
//    fun setPhoneNumber(phoneNumber: String) {
//        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
//    }
//
//    fun onOtpChange(otp: String) {
//        val isValid = otp.length == 6
//
//        _uiState.value = _uiState.value.copy(
//            otp = otp,
//            otpError = "", // Clear error on input change
//            isValidOtp = isValid,
//            hasError = false // Reset error state
//        )
//
//        // Auto submit when OTP is complete and valid
//        if (otp.length == 6) {
//            verifyOtp()
//        }
//    }
//
//    fun verifyOtp() {
//        val currentState = _uiState.value
//
//        if (currentState.otp.length != 6) {
//            _uiState.value = currentState.copy(
//                otpError = "Please enter complete OTP",
//                hasError = true
//            )
//            return
//        }
//
//        viewModelScope.launch {
//            authRepository.verifyOtp(currentState.phoneNumber, currentState.otp)
//                .collect { result ->
//                    when (result) {
//                        is AuthResult.Loading -> {
//                            _uiState.value = currentState.copy(isLoading = true)
//                        }
//                        is AuthResult.Success -> {
//                            _uiState.value = currentState.copy(
//                                isLoading = false,
//                                user = result.user,
//                                navigateToNext = true
//                            )
//                        }
//                        is AuthResult.Error -> {
//                            _uiState.value = currentState.copy(
//                                isLoading = false,
//                                otpError = result.message,
//                                hasError = true
//                            )
//                        }
//                    }
//                }
//        }
//    }
//
//    fun resendOtp() {
//        if (_resendTimer.value == 0) {
//            viewModelScope.launch {
//                authRepository.sendOtp(_uiState.value.phoneNumber)
//                    .collect { result ->
//                        when (result) {
//                            is AuthResult.Success -> {
//                                _resendTimer.value = 45
//                                startResendTimer()
//                                // Clear any existing OTP and errors
//                                _uiState.value = _uiState.value.copy(
//                                    otp = "",
//                                    otpError = "",
//                                    hasError = false
//                                )
//                            }
//                            is AuthResult.Error -> {
//                                _uiState.value = _uiState.value.copy(
//                                    otpError = result.message,
//                                    hasError = true
//                                )
//                            }
//                            is AuthResult.Loading -> {
//                                // Handle loading if needed
//                            }
//                        }
//                    }
//            }
//        }
//    }
//
//    private fun startResendTimer() {
//        viewModelScope.launch {
//            while (_resendTimer.value > 0) {
//                delay(1000)
//                _resendTimer.value = _resendTimer.value - 1
//            }
//        }
//    }
//
//    fun onNavigateToNext() {
//        _uiState.value = _uiState.value.copy(navigateToNext = false)
//    }
//}
//
//data class OtpVerificationUiState(
//    val phoneNumber: String = "",
//    val otp: String = "",
//    val otpError: String = "",
//    val isValidOtp: Boolean = false,
//    val hasError: Boolean = false, // Added to track error state explicitly
//    val isLoading: Boolean = false,
//    val user: User? = null,
//    val navigateToNext: Boolean = false
//)