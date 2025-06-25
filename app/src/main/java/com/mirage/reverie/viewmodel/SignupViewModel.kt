package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirage.reverie.R
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onUsernameChange(newUsername: String) {
        _inputState.update { state ->
            state.copy(username = newUsername)
        }
    }

    fun onEmailChange(newEmail: String) {
        _inputState.update { state ->
            state.copy(email = newEmail)
        }
    }

    fun onNameChange(newName: String) {
        _inputState.update { state ->
            state.copy(name = newName)
        }
    }

    fun onSurnameChange(newSurname: String) {
        _inputState.update { state ->
            state.copy(surname = newSurname)
        }
    }

    fun onPasswordChange(newPassword: String) {
        _inputState.update { state ->
            state.copy(password = newPassword)
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _inputState.update { state ->
            state.copy(confirmPassword = newConfirmPassword)
        }
    }

    fun onSignup() {
        _uiState.update { SignupUiState.Idle }

        val inState = inputState.value

        viewModelScope.launch {
            _inputState.update { state ->
                state.copy(
                    usernameError = "",
                    emailError = "",
                    nameError = "",
                    surnameError = "",
                    passwordError = "",
                    confirmPasswordError = ""
                )
            }

            if (inState.username.isBlank()) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(usernameError = context.getString(R.string.username_mandatory)) }
            } else if (repository.isUsernameTaken(inState.username)) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(usernameError = context.getString(R.string.username_already_taken)) }
            }

            if (inState.email.isBlank()) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(emailError = context.getString(R.string.email_mandatory)) }
            }

            if (inState.name.isBlank()) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(nameError = context.getString(R.string.name_mandatory)) }
            }

            if (inState.surname.isBlank()) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(surnameError = context.getString(R.string.surname_mandatory)) }
            }

            if (inState.password.length < 8) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(passwordError = context.getString(R.string.passwords_lenght)) }
            } else if (inState.password != inState.confirmPassword) {
                _uiState.update { SignupUiState.Error("") }
                _inputState.update { state -> state.copy(confirmPasswordError = context.getString(R.string.passwords_dont_match)) }
            }


            // if uiState is error, we save the error string and return
            if (uiState.value is SignupUiState.Error) {
                return@launch
            }

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

