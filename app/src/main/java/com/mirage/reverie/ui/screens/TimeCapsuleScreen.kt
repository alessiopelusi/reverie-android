package com.mirage.reverie.ui.screens


import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mirage.reverie.ui.theme.PaperColor
import com.mirage.reverie.viewmodel.AllDiariesUiState

import com.mirage.reverie.viewmodel.TimeCapsuleUiState
import com.mirage.reverie.viewmodel.TimeCapsuleViewModel
import androidx.compose.material.icons.filled.Add


@Composable
fun TimeCapsuleScreen(
    viewModel: TimeCapsuleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is TimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is TimeCapsuleUiState.Success -> {
            val timeCapsuleScheduled = (uiState as TimeCapsuleUiState.Success).timeCapsuleScheduled
            val timeCapsuleSent = (uiState as TimeCapsuleUiState.Success).timeCapsuleSent
            val timeCapsuleReceived = (uiState as TimeCapsuleUiState.Success).timeCapsuleReceived

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                item{
                    Card (
                        Modifier
                            .padding(8.dp)
                            .border(width = 2.dp, color = Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Adds spacing between elements
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TimeCapsuleComposable(
                                modifier = Modifier,
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                text = "Lettera"
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "Questa lettera serve per mandare un messaggio nel futuro"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton (
                                    onClick = {
                                        // onNavigateToCreateTimeCapsule()
                                    },
                                    colors = IconButtonColors(
                                        containerColor = PaperColor,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        disabledContentColor = MaterialTheme.colorScheme.primary
                                    ),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription="Create")
                                }
                            }
                        }
                    }
                }
            }
        }
        is TimeCapsuleUiState.Error -> Text(text = "Error: ${(uiState as TimeCapsuleUiState.Error).exception.message}")
    }
}

@Composable
fun TimeCapsuleComposable(modifier: Modifier) {
    Box(
        modifier = modifier
//            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
            contentDescription = null
        )
    }
}