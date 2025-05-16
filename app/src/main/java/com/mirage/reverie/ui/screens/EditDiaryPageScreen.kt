package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.viewmodel.EditDiaryPageUiState
import com.mirage.reverie.viewmodel.EditDiaryPageViewModel

@Composable
fun EditDiaryPageScreen(viewModel: EditDiaryPageViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is EditDiaryPageUiState.Loading -> CircularProgressIndicator()
        is EditDiaryPageUiState.Success -> {
            val page = (uiState as EditDiaryPageUiState.Success).page
            Text(
                modifier = Modifier.padding(8.dp),
                text = "You are editing your diary!",
            )
            EditContentTextField(page.content, onUpdateContent = { newContent -> })//TODO
        }
        is EditDiaryPageUiState.Error -> Text(text = "Error: ${(uiState as EditDiaryPageUiState.Error).exception.message}")
    }
}

@Composable
fun EditContentTextField(title: String, onUpdateContent: (String) -> Unit) {
    TextField(
        value = title,
        onValueChange = onUpdateContent,
        label = { Text("Contenuto") }
    )
}