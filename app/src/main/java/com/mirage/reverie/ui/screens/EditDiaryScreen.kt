package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.viewmodel.DiaryUiState
import com.mirage.reverie.viewmodel.DiaryViewModel
import com.mirage.reverie.viewmodel.EditDiaryUiState
import com.mirage.reverie.viewmodel.EditDiaryViewModel
import com.mirage.reverie.viewmodel.LoginUiState

@Composable
fun EditDiaryScreen(
    viewModel: EditDiaryViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is EditDiaryUiState.Loading -> CircularProgressIndicator()
        is EditDiaryUiState.Idle, is EditDiaryUiState.Error -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            Column {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "You are editing your diary!",
                )
                EditTitleTextField(formState.title, onUpdateTitle = viewModel::onUpdateTitle)
                Button(
                    onClick = viewModel::onUpdateDiary
                ) {
                    Text("Modifica")
                }

                if (uiState is EditDiaryUiState.Error) {
                    Text(text = (uiState as EditDiaryUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is EditDiaryUiState.Success -> {}
    }
}

@Composable
fun EditTitleTextField(title: String, onUpdateTitle: (String) -> Unit) {
    TextField(
        value = title,
        onValueChange = onUpdateTitle,
        label = { Text("Titolo") }
    )
}
