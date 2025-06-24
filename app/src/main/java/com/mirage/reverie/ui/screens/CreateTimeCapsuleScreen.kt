package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.R
import com.mirage.reverie.ui.components.ContentTextField
import com.mirage.reverie.viewmodel.CreateTimeCapsuleUiState
import com.mirage.reverie.viewmodel.CreateTimeCapsuleViewModel
import androidx.compose.runtime.getValue


@Composable
fun CreateTimeCapsuleScreen(
    onComplete: (TimeCapsule) -> Unit,
    viewModel: CreateTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is CreateTimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is CreateTimeCapsuleUiState.Idle, is CreateTimeCapsuleUiState.Error -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            val timeCapsule = formState.timeCapsule

            Column (
                modifier = Modifier.padding(0.dp, 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.create_time_capsule_message),
                )

                EditTitleField(timeCapsule.title, onNewValue = viewModel::onUpdateTitle)
                ContentTextField (timeCapsule.content, onUpdateContent = viewModel::onUpdateContent)

                Button (
                    onClick = viewModel::onCreateTimeCapsule
                ) {
                    Text(stringResource(R.string.create))
                }

                if (uiState is CreateTimeCapsuleUiState.Error) {
                    Text(text = (uiState as CreateTimeCapsuleUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is CreateTimeCapsuleUiState.Success -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            onComplete(formState.timeCapsule)
        }
    }
}