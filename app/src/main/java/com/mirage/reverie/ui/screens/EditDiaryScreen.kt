package com.mirage.reverie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mirage.reverie.R
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.viewmodel.EditDiaryUiState
import com.mirage.reverie.viewmodel.EditDiaryViewModel

@Composable
fun EditDiaryScreen(
    onComplete: (Diary) -> Unit,
    viewModel: EditDiaryViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is EditDiaryUiState.Loading -> CircularProgressIndicator()
        is EditDiaryUiState.Idle, is EditDiaryUiState.Error -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            val diary = formState.diary
            val allCoversMap = formState.allCoversMap
            val selectedCover = diary.coverId

            Column (
                modifier = Modifier.padding(0.dp, 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "You are editing your diary!",
                )
                EditTitleField(diary.title, onNewValue = viewModel::onUpdateTitle)

                LazyVerticalGrid (
                    columns = GridCells.Fixed(3), // 3 columns grid
                    modifier = Modifier
                        .padding(8.dp)
                    //modifier = Modifier.fillMaxSize()
                ) {
                    items(allCoversMap.keys.toList()) { item ->
                        DiaryCoverPreview(
                            coverUrl = allCoversMap.getValue(item).url,
                            modifier = Modifier
                                .padding(1.dp)
                                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
                                .background(
                                    color = if (selectedCover == item) Color.Blue else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.onUpdateCover(item) }
                                .padding(20.dp)
                        )
                    }
                }

                Button(
                    onClick = viewModel::onUpdateDiary
                ) {
                    Text("Salva")
                }

                if (uiState is EditDiaryUiState.Error) {
                    Text(text = (uiState as EditDiaryUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is EditDiaryUiState.Success -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            onComplete(formState.diary)
        }
    }
}

@Composable
fun DiaryCoverPreview(coverUrl: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null
        )
    }
}

@Composable
fun EditTitleField(value: String, onNewValue: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        singleLine = true,
        modifier = modifier,
        value = value,
        onValueChange = { onNewValue(it) },
        placeholder = { Text(stringResource(R.string.title)) },
        //leadingIcon = { [...] }
    )
}
