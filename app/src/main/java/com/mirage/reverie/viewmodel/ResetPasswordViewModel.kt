package com.mirage.reverie.viewmodel

import androidx.lifecycle.ViewModel
import com.mirage.reverie.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ResetPasswordInputState(
    val email: String = "",
)

sealed class ResetPasswordUiState {
    data object Idle : ResetPasswordUiState()
    data class Success(val infoMessage: String) : ResetPasswordUiState()
    data class Error(val errorMessage: String) : ResetPasswordUiState()
}


@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _inputState = MutableStateFlow(ResetPasswordInputState())
    val inputState = _inputState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _inputState.update { state ->
            state.copy(email = newEmail)
        }
    }

    fun onResetPassword() {
        _uiState.update { ResetPasswordUiState.Idle }

        val state = inputState.value
        if (state.email.isBlank()) {
            _uiState.update { ResetPasswordUiState.Error("L'Email è obbligatoria") }
        }

        accountService.sendPasswordResetEmail(state.email) { exception ->
            if (exception == null) {
                val infoMessage = "Email per reset password inviata"
                _uiState.update { ResetPasswordUiState.Success(infoMessage) }
            } else {
                val errorMessage = exception.message ?: "Errore nell'invio dell'email"
                _uiState.update { ResetPasswordUiState.Error(errorMessage) }
            }
        }
    }
}

