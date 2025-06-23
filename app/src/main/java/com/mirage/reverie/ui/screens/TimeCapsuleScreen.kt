package com.mirage.reverie.ui.screens


import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mirage.reverie.viewmodel.TimeCapsuleUiState
import com.mirage.reverie.viewmodel.TimeCapsuleViewModel


@Composable
fun TimeCapsuleScreen(
    viewModel: TimeCapsuleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is TimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is TimeCapsuleUiState.Success -> {

        }
        is TimeCapsuleUiState.Error -> Text(text = "Error: ${(uiState as TimeCapsuleUiState.Error).exception.message}")
    }
}