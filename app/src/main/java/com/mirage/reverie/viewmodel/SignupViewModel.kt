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

    init {

    }

    private var usernameCheckJob: Job? = null

    fun onUsernameChange(newUsername: String) {
        _inputState.update { state ->
            state.copy(
                username = newUsername,
                usernameError = ""
            )
        }

        // Cancel any ongoing username check
        usernameCheckJob?.cancel()

        // Start a new coroutine for validation
        usernameCheckJob = viewModelScope.launch {
            var error = ""

            if (newUsername.isBlank()) {
                _uiState.update { SignupUiState.Error("") }
                error = context.getString(R.string.username_mandatory)
            } else if (repository.isUsernameTaken(newUsername)) {
                _uiState.update { SignupUiState.Error("") }
                error = context.getString(R.string.username_already_taken)
            }

            _inputState.update { state ->
                state.copy(
                    usernameError = error
                )
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        var error = ""

        if (newEmail.isBlank()) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.email_mandatory)
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.email_not_valid)
        }

        _inputState.update { state ->
            state.copy(
                email = newEmail,
                emailError = error
            )
        }
    }

    fun onNameChange(newName: String) {
        var error = ""

        if (newName.isBlank()) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.name_mandatory)
        }

        _inputState.update { state ->
            state.copy(
                name = newName,
                nameError = error
            )
        }
    }

    fun onSurnameChange(newSurname: String) {
        var error = ""

        if (newSurname.isBlank()) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.surname_mandatory)
        }

        _inputState.update { state ->
            state.copy(
                surname = newSurname,
                surnameError = error
            )
        }
    }

    fun onPasswordChange(newPassword: String) {
        var error = ""

        if (newPassword.length < 8) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.passwords_lenght)
        }

        _inputState.update { state ->
            state.copy(
                password = newPassword,
                passwordError = error
            )
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        var error = ""

        if (inputState.value.password.isNotBlank() && newConfirmPassword != inputState.value.password) {
            _uiState.update { SignupUiState.Error("") }
            error = context.getString(R.string.passwords_dont_match)
        }

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

        onUsernameChange(inState.username)
        onEmailChange(inState.email)
        onNameChange(inState.name)
        onSurnameChange(inState.surname)
        onPasswordChange(inState.password)
        onConfirmPasswordChange(inState.confirmPassword)

        // if uiState is error, we save the error string and return
        if (uiState.value is SignupUiState.Error) {
            return
        }

        viewModelScope.launch {
            // otherwise we save the profile and go back to login
            val user = repository.createAccount(
                User(
                    email = inState.email,
                    username = inState.username,
                    name = inState.name,
                    surname = inState.surname,
                ),
                inState.password
            )

            if (user != null) {
                _uiState.update { SignupUiState.Error(context.getString(R.string.signup_error)) }
            } else {
                _uiState.update { SignupUiState.Success(context.getString(R.string.signup_successful)) }
            }
        }
    }
}

