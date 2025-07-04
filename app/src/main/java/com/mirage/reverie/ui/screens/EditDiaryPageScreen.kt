package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        is EditDiaryPageUiState.Idle -> {
            val page = formState.page

            Column (
                modifier = Modifier.padding(50.dp, 20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ){
                Text(
                    text = stringResource(R.string.edit_diary_page_message),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                ContentTextField(page.content, viewModel::onUpdateContent, stringResource(R.string.content))

                Button(
                    onClick = viewModel::onUpdatePage
                ) {
                    Text(stringResource(R.string.edit_diary))
                }
            }
        }
        is EditDiaryPageUiState.Success -> {
            onComplete(formState.page)
        }
    }
}
