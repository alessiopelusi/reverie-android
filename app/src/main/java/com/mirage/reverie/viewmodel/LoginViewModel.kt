package com.mirage.reverie.viewmodel

import androidx.lifecycle.ViewModel
import com.mirage.reverie.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginInputState(
    val email: String = "",
    val password: String = ""
)

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val errorMessage: String) : LoginUiState()
}


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService
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

        val state = inputState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { LoginUiState.Error("Email e password sono obbligatori") }
        }

        accountService.authenticate(state.email, state.password) { exception ->
            if (exception == null) {
                _uiState.update { LoginUiState.Success }
            } else {
                val errorMessage = exception.message ?: "Errore di autenticazione"
                _uiState.update { LoginUiState.Error(errorMessage) }
            }
        }
    }
}

