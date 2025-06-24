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
import com.mirage.reverie.ui.components.Field
import com.mirage.reverie.ui.components.PasswordField
import com.mirage.reverie.viewmodel.LoginUiState
import com.mirage.reverie.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    when (uiState) {
        is LoginUiState.Success -> {
            onLoginSuccess()
        }
        is LoginUiState.Idle, is LoginUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.login), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Field(inputState.email, viewModel::onEmailChange, R.string.email)

                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(inputState.password, viewModel::onPasswordChange)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onLogin
                ) {
                    Text(stringResource(R.string.login))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(stringResource(R.string.go_to_signup))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateToResetPassword) {
                    Text(stringResource(R.string.forgot_password))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is LoginUiState.Error) {
                    Text(text = (uiState as LoginUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

}