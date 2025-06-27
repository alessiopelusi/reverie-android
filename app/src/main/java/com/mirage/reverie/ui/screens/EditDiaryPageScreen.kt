package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.R
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.ui.components.ContentTextField
import com.mirage.reverie.viewmodel.EditDiaryPageUiState
import com.mirage.reverie.viewmodel.EditDiaryPageViewModel

@Composable
fun EditDiaryPageScreen(
    onComplete: (DiaryPage) -> Unit,
    viewModel: EditDiaryPageViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    when(uiState) {
        is EditDiaryPageUiState.Loading -> CircularProgressIndicator()
        is EditDiaryPageUiState.Idle, is EditDiaryPageUiState.Error -> {
            val page = formState.page


            Column (
                modifier = Modifier.padding(0.dp, 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "You are editing your diary!",
                )
                ContentTextField(page.content, viewModel::onUpdateContent, stringResource(R.string.content))

                Button(
                    onClick = viewModel::onUpdatePage
                ) {
                    Text("Modifica")
                }
            }
        }
        is EditDiaryPageUiState.Success -> {
            onComplete(formState.page)
        }
    }
}
