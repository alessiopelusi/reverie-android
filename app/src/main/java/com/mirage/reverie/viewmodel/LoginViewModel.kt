package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mirage.reverie.R
import com.mirage.reverie.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginInputState(
    val email: String = "",
    val emailError: String = "",
    val password: String = "",
    val passwordError: String = ""
)

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val errorMessage: String) : LoginUiState()
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _inputState = MutableStateFlow(LoginInputState())
    val inputState = _inputState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _inputState.update { state ->
            state.copy(email = newEmail)
        }
    }

    fun onPasswordChange(newPassword: String) {
        _inputState.update { state ->
            state.copy(password = newPassword)
        }
    }

    fun onLogin() {
        _uiState.update { LoginUiState.Idle }

        val inState = inputState.value

        _inputState.update { state ->
            state.copy(
                emailError = "",
                passwordError = "",
            )
        }

        if (inState.email.isBlank()) {
            _uiState.update { LoginUiState.Error("") }
            _inputState.update { state -> state.copy(emailError = context.getString(R.string.email_mandatory)) }
        }

        if (inState.password.isBlank()) {
            _uiState.update { LoginUiState.Error("") }
            _inputState.update { state -> state.copy(passwordError = context.getString(R.string.password_mandatory)) }
        }

        // if uiState is error, we save the error string and return
        if (uiState.value is LoginUiState.Error) {
            return
        }

        // otherwise we login
        viewModelScope.launch {
            if (repository.authenticate(inState.email, inState.password)) {
                _uiState.update { LoginUiState.Success }
            } else {
                _uiState.update { LoginUiState.Error(context.getString(R.string.login_error)) }
            }
        }
    }
}

