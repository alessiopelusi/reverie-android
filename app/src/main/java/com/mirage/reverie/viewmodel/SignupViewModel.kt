package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirage.reverie.R
import com.mirage.reverie.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SignupInputState(
    val username: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

sealed class SignupUiState {
    data object Idle : SignupUiState()
    data class Success(val infoMessage: String = "") : SignupUiState()
    data class Error(val errorMessage: String = "") : SignupUiState()
}


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountRepository,
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

        val state = inputState.value
        val error = StringBuilder()

        if (state.email.isBlank()) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.email_mandatory))
        }
        if (state.username.isBlank()) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.username_mandatory))
        }
        if (state.name.isBlank()) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.name_mandatory))
        }
        if (state.surname.isBlank()) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.surname_mandatory))
        }

        if (state.password.length < 8) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.passwords_lenght))
        } else if (state.password != state.confirmPassword) {
            _uiState.update { SignupUiState.Error() }
            error.appendLine(context.getString(R.string.passwords_dont_match))
        }

        // if uiState is error, we save the error string and return
        if (uiState.value is SignupUiState.Error) {
            _uiState.update { SignupUiState.Error(error.toString()) }
            return
        }

        // otherwise we save the profile and go back to login

        viewModelScope.launch {
            accountService.createAccount(
                state.username,
                state.email,
                state.password
            ) { exception ->
                if (exception == null) {
                    _uiState.update { SignupUiState.Success(context.getString(R.string.signup_successful)) }
                } else {
                    val errorMessage = exception.message ?: context.getString(R.string.signup_error)
                    _uiState.update { SignupUiState.Error(errorMessage) }
                }
            }
        }
    }
}

