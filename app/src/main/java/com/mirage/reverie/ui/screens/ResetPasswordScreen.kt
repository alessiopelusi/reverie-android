package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.R
import com.mirage.reverie.ui.components.EmailField
import com.mirage.reverie.ui.components.PasswordField
import com.mirage.reverie.viewmodel.LoginUiState
import com.mirage.reverie.viewmodel.LoginViewModel
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
                Text(text = stringResource(R.string.login), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                EmailField(inputState.email, viewModel::onEmailChange)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::onResetPassword
                ) {
                    Text("Invia email di reset")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateBack) {
                    Text("Torna al login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is ResetPasswordUiState.Error) {
                    Text(text = (uiState as ResetPasswordUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

}


@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Reset Password", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
                infoMessage = ""
            },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                errorMessage = ""
                infoMessage = ""

                if (email.isBlank()) {
                    errorMessage = "Inserisci la tua email"
                    return@Button
                }

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            infoMessage = "Email per reset password inviata"
                        } else {
                            errorMessage = task.exception?.message ?: "Errore nell'invio dell'email"
                        }
                    }
            }
        ) {
            Text("Invia email di reset")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Torna al login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        if (infoMessage.isNotEmpty()) {
            Text(text = infoMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}