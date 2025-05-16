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
import com.mirage.reverie.viewmodel.DiaryUiState
import com.mirage.reverie.viewmodel.DiaryViewModel

@Composable
fun EditDiaryScreen(viewModel: DiaryViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is DiaryUiState.Loading -> CircularProgressIndicator()
        is DiaryUiState.Success -> {
            val diary = (uiState as DiaryUiState.Success).diary
            Text(
                modifier = Modifier.padding(8.dp),
                text = "You are editing your diary!",
            )
            EditTitleTextField(diary.title, onUpdateTitle = { viewModel.changeTitle(it) })
        }
        is DiaryUiState.Error -> Text(text = "Error: ${(uiState as DiaryUiState.Error).exception.message}")
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
