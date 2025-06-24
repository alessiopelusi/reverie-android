package com.mirage.reverie.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

import com.mirage.reverie.viewmodel.TimeCapsuleUiState
import com.mirage.reverie.viewmodel.TimeCapsuleViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.ui.theme.Purple80
import com.mirage.reverie.viewmodel.TimeCapsuleButtonState
import java.text.SimpleDateFormat


@Composable
fun TimeCapsuleScreen(
    viewModel: TimeCapsuleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is TimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is TimeCapsuleUiState.Success -> {
            val timeCapsuleScheduledMap = (uiState as TimeCapsuleUiState.Success).timeCapsuleScheduledMap
            val timeCapsuleScheduled = (uiState as TimeCapsuleUiState.Success).timeCapsuleScheduled
            val timeCapsuleSentMap = (uiState as TimeCapsuleUiState.Success).timeCapsuleSentMap
            val timeCapsuleSent = (uiState as TimeCapsuleUiState.Success).timeCapsuleSent
            val timeCapsuleReceivedMap = (uiState as TimeCapsuleUiState.Success).timeCapsuleReceivedMap
            val timeCapsuleReceived = (uiState as TimeCapsuleUiState.Success).timeCapsuleReceived

            val buttonElements = (uiState as TimeCapsuleUiState.Success).buttonElements
            val buttonState = (uiState as TimeCapsuleUiState.Success).buttonState

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
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val cornerRadius = 16.dp

                        buttonElements.forEachIndexed { index, item ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.onButtonStateUpdate(item)
                                },
                                shape = when (index) {
                                    0 -> RoundedCornerShape(
                                        topStart = cornerRadius,
                                        topEnd = 0.dp,
                                        bottomStart = cornerRadius,
                                        bottomEnd = 0.dp
                                    )

                                    buttonElements.size - 1 -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = cornerRadius,
                                        bottomStart = 0.dp,
                                        bottomEnd = cornerRadius
                                    )

                                    else -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                    )
                                },
                                border = BorderStroke(
                                    1.dp, if (buttonState == item) {
                                        Purple80
                                    } else {
                                        Purple80.copy(alpha = 0.75f)
                                    }
                                ),
                                colors = if (buttonState == item) {
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = Purple80.copy(alpha = 0.1f),
                                        contentColor = Purple80
                                    )
                                } else {
                                    ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        contentColor = Purple80
                                    )
                                }
                            ) {
                                Text(item.toString())
                            }

                        }
                    }
                }
                when(buttonState) {
                    TimeCapsuleButtonState.SCHEDULED -> {
                        items(timeCapsuleScheduled) { timeCapsule ->
                            ScheduledTimeCapsule(timeCapsule)
                        }
                    }
                    TimeCapsuleButtonState.SENT -> {
                        items(timeCapsuleSent) { timeCapsule ->
                            SentTimeCapsule(timeCapsule)
                        }
                    }
                    TimeCapsuleButtonState.RECEIVED -> {
                        items(timeCapsuleReceived) { timeCapsule ->
                            ReceivedTimeCapsule(timeCapsule)
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

@Composable
fun ScheduledTimeCapsule(timeCapsule: TimeCapsule) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format

    Row(
        modifier = Modifier.padding(20.dp, 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Column {
            Text(timeCapsule.title)
            Text((timeCapsule.emails + timeCapsule.phones + timeCapsule.receivers).toString())
            Text("Created: " + formatter.format(timeCapsule.creationDate.toDate()))
            Text("Deadline: " + formatter.format(timeCapsule.deadline.toDate()))
        }
    }
}

@Composable
fun SentTimeCapsule(timeCapsule: TimeCapsule) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format

    Row(
        modifier = Modifier.padding(20.dp, 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Column {
            Text(timeCapsule.title)
            Text((timeCapsule.emails + timeCapsule.phones + timeCapsule.receivers).toString())
            Text("Sent: " + formatter.format(timeCapsule.deadline.toDate()))
        }
    }
}

@Composable
fun ReceivedTimeCapsule(timeCapsule: TimeCapsule) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format

    Row(
        modifier = Modifier.padding(20.dp, 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
            contentDescription = null,
            modifier = Modifier.size(80.dp)
        )
        Column {
            Text(timeCapsule.title)
            Text(timeCapsule.userId)
            Text("Received: " + formatter.format(timeCapsule.deadline.toDate()))
        }
    }
}
