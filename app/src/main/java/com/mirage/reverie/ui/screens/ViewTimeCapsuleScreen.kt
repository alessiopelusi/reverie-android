package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.ui.components.ContentTextField
import com.mirage.reverie.viewmodel.ViewTimeCapsuleUiState
import com.mirage.reverie.viewmodel.ViewTimeCapsuleViewModel

@Composable
fun ViewTimeCapsuleScreen(
    onComplete: (TimeCapsule) -> Unit,
    viewModel: ViewTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    when(uiState) {
        is ViewTimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is ViewTimeCapsuleUiState.Idle, is ViewTimeCapsuleUiState.Error -> {
            val timeCapsule = formState.timeCapsule

            Column (
                modifier = Modifier.padding(0.dp, 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "You are editing your diary!",
                )
                ContentTextField (timeCapsule.content, onUpdateContent = viewModel::onUpdateContent)

                Button(
                    onClick = viewModel::onUpdateTimeCapsule
                ) {
                    Text("Modifica")
                }
            }
        }
        is ViewTimeCapsuleUiState.Success -> {
            onComplete(formState.timeCapsule)
        }
    }
}
