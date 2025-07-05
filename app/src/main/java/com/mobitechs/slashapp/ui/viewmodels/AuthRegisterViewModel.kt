package com.mobitechs.slashapp.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.data.model.RegisterUserRequest
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.utils.ValidationResult
import com.mobitechs.slashapp.utils.isValidEmail
import com.mobitechs.slashapp.utils.isValidOptional
import com.mobitechs.slashapp.utils.isValidPhone
import com.mobitechs.slashapp.utils.isValidRequired
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthRegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val emailId: String = "",
    val referralCode: String = "",
    val isTermsAccepted: Boolean = false,

    // Simple validation states
    val firstNameValidation: ValidationResult = ValidationResult(false),
    val lastNameValidation: ValidationResult = ValidationResult(false),
    val phoneValidation: ValidationResult = ValidationResult(false),
    val emailValidation: ValidationResult = ValidationResult(false),
    val referralCodeValidation: ValidationResult = ValidationResult(true), // Optional

    val isLoading: Boolean = false,
    val error: String = "",
    val navigateToOtp: Boolean = false
) {
    val isFormValid: Boolean
        get() = firstNameValidation.isValid &&
                lastNameValidation.isValid &&
                phoneValidation.isValid &&
                emailValidation.isValid &&
                referralCodeValidation.isValid && isTermsAccepted
}

class AuthRegisterViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AuthRegisterUiState())
    val uiState: StateFlow<AuthRegisterUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(firstName: String) {
        val validation = firstName.isValidRequired("First name")
        _uiState.update {
            it.copy(
                firstName = firstName,
                firstNameValidation = validation,
                error = ""
            )
        }
    }

    fun onLastNameChange(lastName: String) {
        val validation = lastName.isValidRequired("Last name")
        _uiState.update {
            it.copy(
                lastName = lastName,
                lastNameValidation = validation,
                error = ""
            )
        }
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        // Allow only digits and limit to 10
        val filteredPhone = phoneNumber.filter { it.isDigit() }.take(10)
        val validation = filteredPhone.isValidPhone()

        _uiState.update {
            it.copy(
                phoneNumber = filteredPhone,
                phoneValidation = validation,
                error = ""
            )
        }
    }

    fun onEmailIdChange(emailId: String) {
        val validation = emailId.isValidEmail()
        _uiState.update {
            it.copy(
                emailId = emailId,
                emailValidation = validation,
                error = ""
            )
        }
    }

    fun onReferralCodeChange(referralCode: String) {
        val validation = referralCode.isValidOptional("Referral code", 10)
        _uiState.update {
            it.copy(
                referralCode = referralCode,
                referralCodeValidation = validation,
                error = ""
            )
        }
    }

    fun onTermsAcceptedChange(isAccepted: Boolean) {
        _uiState.update {
            it.copy(
                isTermsAccepted = isAccepted,
                error = ""
            )
        }
    }


    fun registerAPICall() {
        val currentState = uiState.value

        // Force validation of all fields
        val firstNameValidation = currentState.firstName.isValidRequired("First name")
        val lastNameValidation = currentState.lastName.isValidRequired("Last name")
        val phoneValidation = currentState.phoneNumber.isValidPhone()
        val emailValidation = currentState.emailId.isValidEmail()
        val referralValidation = currentState.referralCode.isValidOptional("Referral code", 10)

        _uiState.update {
            it.copy(
                firstNameValidation = firstNameValidation,
                lastNameValidation = lastNameValidation,
                phoneValidation = phoneValidation,
                emailValidation = emailValidation,
                referralCodeValidation = referralValidation
            )
        }

        if (!uiState.value.isFormValid) {
            return
        }

        _uiState.update { it.copy(isLoading = true, error = "") }

        viewModelScope.launch {
            try {
                val response = authRepository.register(RegisterUserRequest(currentState.phoneNumber,
                    currentState.firstName,currentState.lastName,currentState.emailId,currentState.referralCode))

                if (response.success == true) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateToOtp = true,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message ?: "Failed to signup"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onNavigateToOtp() {
        _uiState.update { it.copy(navigateToOtp = false) }
    }
}
