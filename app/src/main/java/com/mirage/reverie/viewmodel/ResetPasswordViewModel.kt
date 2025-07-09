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
    val emailError: String = "",
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

    private fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> context.getString(R.string.email_mandatory)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> context.getString(R.string.email_not_valid)
            else -> ""
        }
    }
    fun onEmailChange(newEmail: String) {
        val strippedEmail = newEmail.trim()
        if (strippedEmail == inputState.value.email) return

        val error = validateEmail(strippedEmail)

        _inputState.update { state ->
            state.copy(
                email = strippedEmail,
                emailError = error
            )
        }
    }

    fun onResetPassword() {
        _uiState.update { ResetPasswordUiState.Idle }

        val inState = inputState.value

        val updatedState = inState.copy(
            emailError = validateEmail(inState.email)
        )
        _inputState.update { updatedState }

        // if we detect an error, we return
        if (updatedState.emailError.isNotBlank()) {
            _uiState.update { ResetPasswordUiState.Error("") }
            return
        }

        if (repository.sendPasswordResetEmail(updatedState.email))
            _uiState.update { ResetPasswordUiState.Success(context.getString(R.string.reset_password_email_sent)) }
        else
            _uiState.update { ResetPasswordUiState.Error(context.getString(R.string.reset_password_email_error)) }
    }
}

