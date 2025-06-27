package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirage.reverie.R
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SignupInputState(
    val username: String = "",
    val usernameError: String = "",
    val email: String = "",
    val emailError: String = "",
    val name: String = "",
    val nameError: String = "",
    val surname: String = "",
    val surnameError: String = "",
    val password: String = "",
    val passwordError: String = "",
    val confirmPassword: String = "",
    val confirmPasswordError: String = "",
)

sealed class SignupUiState {
    data object Idle : SignupUiState()
    data class Success(val infoMessage: String = "") : SignupUiState()
    data class Error(val errorMessage: String) : SignupUiState()
}


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repository: UserRepository,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _inputState = MutableStateFlow(SignupInputState())
    val inputState = _inputState.asStateFlow()

    private suspend fun validateUsername(username: String): String {
        return when {
            username.isBlank() -> context.getString(R.string.username_mandatory)
            username.length < 3 -> context.getString(R.string.username_length)
            repository.isUsernameTaken(username) -> context.getString(R.string.username_already_taken)
            else -> ""
        }
    }

    private suspend fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> context.getString(R.string.email_mandatory)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> context.getString(R.string.email_not_valid)
            repository.isEmailTaken(email) -> context.getString(R.string.email_already_taken)
            else -> ""
        }
    }

    private fun validateName(name: String): String {
        return if (name.isBlank()) {
            context.getString(R.string.name_mandatory)
        } else {
            ""
        }
    }

    private fun validateSurname(surname: String): String {
        return if (surname.isBlank()) {
            context.getString(R.string.surname_mandatory)
        } else {
            ""
        }
    }

    private fun validatePassword(password: String): String {
        return if (password.length < 8) {
            context.getString(R.string.password_length)
        } else {
            ""
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String {
        return if (password.isNotBlank() && password != confirmPassword) {
            context.getString(R.string.passwords_dont_match)
        } else {
            ""
        }
    }

    private var usernameCheckJob: Job? = null

    fun onUsernameChange(newUsername: String) {
        val strippedUsername = newUsername.trim()
        if (strippedUsername == inputState.value.username) return

        _inputState.update { state ->
            state.copy(
                username = strippedUsername,
            )
        }

        // Cancel any ongoing username check
        usernameCheckJob?.cancel()

        // Start a new coroutine for validation
        usernameCheckJob = viewModelScope.launch {
            val error = validateUsername(strippedUsername)

            _inputState.update { state ->
                state.copy(
                    usernameError = error
                )
            }
        }
    }

    private var emailCheckJob: Job? = null

    fun onEmailChange(newEmail: String) {
        val strippedEmail = newEmail.trim()
        if (strippedEmail == inputState.value.email) return

        _inputState.update { state ->
            state.copy(
                email = strippedEmail,
            )
        }

        // Cancel any ongoing username check
        emailCheckJob?.cancel()

        // Start a new coroutine for validation
        emailCheckJob = viewModelScope.launch {
            val error = validateEmail(strippedEmail)

            _inputState.update { state ->
                state.copy(
                    emailError = error
                )
            }
        }
    }

    fun onNameChange(newName: String) {
        val strippedName = newName.trim()
        if (strippedName == inputState.value.name) return

        val error = validateName(strippedName)

        _inputState.update { state ->
            state.copy(
                name = strippedName,
                nameError = error
            )
        }
    }

    fun onSurnameChange(newSurname: String) {
        val strippedSurname = newSurname.trim()
        if (strippedSurname == inputState.value.surname) return

        val error = validateSurname(strippedSurname)

        _inputState.update { state ->
            state.copy(
                surname = strippedSurname,
                surnameError = error
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        val error = validatePassword(newPassword)

        _inputState.update { state ->
            state.copy(
                password = newPassword,
                passwordError = error
            )
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        val error = validateConfirmPassword(inputState.value.password, newConfirmPassword)

        _inputState.update { state ->
            state.copy(
                confirmPassword = newConfirmPassword,
                confirmPasswordError = error
            )
        }
    }

    fun onSignup() {
        _uiState.update { SignupUiState.Idle }

        val inState = inputState.value

        viewModelScope.launch {
            // Validate all fields and update input state
            val updatedState = inState.copy(
                usernameError = validateUsername(inState.username),
                emailError = validateEmail(inState.email),
                nameError = validateName(inState.name),
                surnameError = validateSurname(inState.surname),
                passwordError = validatePassword(inState.password),
                confirmPasswordError = validateConfirmPassword(
                    inState.password,
                    inState.confirmPassword
                )
            )
            _inputState.update { updatedState }

            // Check for any validation errors
            if (listOf(
                    updatedState.usernameError,
                    updatedState.emailError,
                    updatedState.nameError,
                    updatedState.surnameError,
                    updatedState.passwordError,
                    updatedState.confirmPasswordError
                ).any { it.isNotBlank() } // Check for non-empty errors
            ) {
                _uiState.update { SignupUiState.Error("") }
                return@launch
            }

            // Proceed to account creation
            val user = repository.createAccount(
                User(
                    email = updatedState.email,
                    username = updatedState.username,
                    name = updatedState.name,
                    surname = updatedState.surname,
                ),
                updatedState.password
            )

            if (user == null) {
                _uiState.update { SignupUiState.Error(context.getString(R.string.signup_error)) }
            } else {
                _uiState.update { SignupUiState.Success(context.getString(R.string.signup_successful)) }
            }
        }
    }
}