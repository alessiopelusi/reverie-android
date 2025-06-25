package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mirage.reverie.R
import com.mirage.reverie.data.repository.UserRepository
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
    private val repository: UserRepository,
    private val context: Context
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
            _uiState.update { ResetPasswordUiState.Error(context.getString(R.string.email_mandatory)) }
            return
        }

        repository.sendPasswordResetEmail(state.email) { exception ->
            if (exception == null) {
                val infoMessage = context.getString(R.string.reset_password_email_sent)
                _uiState.update { ResetPasswordUiState.Success(infoMessage) }
            } else {
                val errorMessage = exception.message ?: context.getString(R.string.reset_password_email_error)
                _uiState.update { ResetPasswordUiState.Error(errorMessage) }
            }
        }
    }
}

