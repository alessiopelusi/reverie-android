package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.R
import com.mirage.reverie.ui.components.ErrorField
import com.mirage.reverie.ui.components.Field
import com.mirage.reverie.ui.components.PasswordField
import com.mirage.reverie.viewmodel.SignupUiState
import com.mirage.reverie.viewmodel.SignupViewModel


@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    when (uiState) {
        is SignupUiState.Success -> {
            onSignupSuccess()
        }
        is SignupUiState.Idle, is SignupUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.signup), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Field(inputState.username, inputState.usernameError, viewModel::onUsernameChange, stringResource(R.string.username))

                Spacer(modifier = Modifier.height(8.dp))

                Field(inputState.email, inputState.emailError, viewModel::onEmailChange, stringResource(R.string.email))

                Spacer(modifier = Modifier.height(8.dp))

                Field(inputState.name, inputState.nameError, viewModel::onNameChange, stringResource(R.string.name))

                Spacer(modifier = Modifier.height(8.dp))

                Field(inputState.surname, inputState.surnameError, viewModel::onSurnameChange, stringResource(R.string.surname))

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(inputState.password, inputState.passwordError, viewModel::onPasswordChange)

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = inputState.confirmPassword,
                    errorMessage = inputState.confirmPasswordError,
                    onNewValue = viewModel::onConfirmPasswordChange,
                    label = stringResource(R.string.confirm_password))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onSignup
                ) {
                    Text(stringResource(R.string.signup))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(stringResource(R.string.already_have_account))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is SignupUiState.Error) {
                    ErrorField((uiState as SignupUiState.Error).errorMessage)
                }
            }
        }
    }

}