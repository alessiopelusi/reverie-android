package com.mirage.reverie.viewmodel

import androidx.lifecycle.ViewModel
import com.mirage.reverie.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class SignupInputState(
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

sealed class SignupUiState {
    data object Idle : SignupUiState()
    data class Success(val infoMessage: String) : SignupUiState()
    data class Error(val errorMessage: String) : SignupUiState()
}


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountService
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

    fun onPasswordChange(newPassword: String) {
        _inputState.update { state ->
            state.copy(password = newPassword)
        }
    }

    fun onSignup() {
        _uiState.update { SignupUiState.Idle }

        val state = inputState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { SignupUiState.Error("Email e password sono obbligatori") }
        }

        accountService.createAccount(state.email, state.password) { exception ->
            if (exception == null) {
                val infoMessage = "Registrazione avvenuta con successo. Torna al login."
                _uiState.update { SignupUiState.Success(infoMessage) }
            } else {
                val errorMessage = exception.message ?: "Errore di registrazione"
                _uiState.update { SignupUiState.Error(errorMessage) }
            }
        }
    }
}

