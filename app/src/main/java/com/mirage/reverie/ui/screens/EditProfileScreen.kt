package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.R
import com.mirage.reverie.data.model.User
import com.mirage.reverie.ui.components.ErrorField
import com.mirage.reverie.ui.components.Field
import com.mirage.reverie.viewmodel.EditProfileUiState
import com.mirage.reverie.viewmodel.EditProfileViewModel
import com.mirage.reverie.viewmodel.SignupUiState


@Composable
fun EditProfileScreen(
    onComplete: (User) -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    when (uiState) {
        is EditProfileUiState.Loading -> CircularProgressIndicator()
        is EditProfileUiState.Idle, is EditProfileUiState.InputError -> {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(R.string.your_profile),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                Field(inputState.username, inputState.usernameError, viewModel::onUsernameChange, stringResource(R.string.username))

                Spacer(modifier = Modifier.height(16.dp))

                Field(inputState.name, inputState.nameError, viewModel::onNameChange, stringResource(R.string.name))

                Spacer(modifier = Modifier.height(16.dp))

                Field(inputState.surname, inputState.surnameError, viewModel::onSurnameChange, stringResource(R.string.surname))

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = {
                    viewModel.onSaveProfile()
                }) {
                    Text(stringResource(R.string.save_changes))
                }

                if (uiState is EditProfileUiState.InputError) {
                    ErrorField((uiState as EditProfileUiState.InputError).errorMessage)
                }
            }
        }
        is EditProfileUiState.Complete -> {
            onComplete((uiState as EditProfileUiState.Complete).profile)
        }
        is EditProfileUiState.LoadingError -> Text(text = "Error: ${(uiState as EditProfileUiState.LoadingError).exception.message}")
    }
}