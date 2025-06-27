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
import com.mirage.reverie.ui.components.SingleLineField
import com.mirage.reverie.viewmodel.ResetPasswordUiState
import com.mirage.reverie.viewmodel.ResetPasswordViewModel

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    when (uiState) {
        is ResetPasswordUiState.Success -> {

        }
        is ResetPasswordUiState.Idle, is ResetPasswordUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.reset_password), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                SingleLineField(inputState.email, viewModel::onEmailChange, stringResource(R.string.email))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onResetPassword
                ) {
                    Text(stringResource(R.string.send_reset_password_email))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(R.string.go_back_to_login))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is ResetPasswordUiState.Error) {
                    Text(text = (uiState as ResetPasswordUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

}
