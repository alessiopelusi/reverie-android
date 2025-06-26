package com.mirage.reverie.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

import com.mirage.reverie.viewmodel.AllTimeCapsulesUiState
import com.mirage.reverie.viewmodel.AllTimeCapsulesViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mirage.reverie.R
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.ui.components.ButtonBar
import com.mirage.reverie.ui.components.ConfirmDelete
import com.mirage.reverie.ui.theme.Purple80
import com.mirage.reverie.viewmodel.TimeCapsuleType
import java.text.SimpleDateFormat


@Composable
fun AllTimeCapsulesScreen(
    newTimeCapsule: TimeCapsule?,
    onNavigateToCreateTimeCapsule: () -> Unit,
    onNavigateToViewTimeCapsule: (String, TimeCapsuleType) -> Unit,
    viewModel: AllTimeCapsulesViewModel = hiltViewModel()
) {
    // add new time capsule sent from CreateTimeCapsuleRoute
    viewModel.addNewTimeCapsule(newTimeCapsule)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is AllTimeCapsulesUiState.Loading -> CircularProgressIndicator()
        is AllTimeCapsulesUiState.Success -> {
            val timeCapsuleScheduled = (uiState as AllTimeCapsulesUiState.Success).timeCapsuleScheduled
            val timeCapsuleSent = (uiState as AllTimeCapsulesUiState.Success).timeCapsuleSent
            val timeCapsuleReceived = (uiState as AllTimeCapsulesUiState.Success).timeCapsuleReceived

            val buttonElements = (uiState as AllTimeCapsulesUiState.Success).buttonElements
            val buttonState = (uiState as AllTimeCapsulesUiState.Success).buttonState

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                item{
                    Card (
                        Modifier.padding(40.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp), // Adds spacing between elements
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TimeCapsuleComposable(
                                modifier = Modifier,
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                text = "Lettera per il futuro"
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "Questa lettera serve per mandare un messaggio nel futuro"
                            )
                        }
                    }
                }
                item {
                    ButtonBar(buttonState, buttonElements, viewModel::onButtonStateUpdate)
                }
                when(buttonState) {
                    TimeCapsuleType.SCHEDULED -> {
                        items(timeCapsuleScheduled) { timeCapsule ->
                            ScheduledTimeCapsule(timeCapsule, onNavigateToViewTimeCapsule) {
                                viewModel.onOpenDeleteTimeCapsuleDialog(timeCapsule.id)
                            }
                            if (timeCapsule != timeCapsuleSent.last()){
                                HorizontalDivider(thickness = 1.dp)
                            }
                        }
                    }
                    TimeCapsuleType.SENT -> {
                        items(timeCapsuleSent) { timeCapsule ->
                            SentTimeCapsule(timeCapsule, onNavigateToViewTimeCapsule)
                            if (timeCapsule != timeCapsuleSent.last()){
                                HorizontalDivider(thickness = 1.dp)
                            }
                        }
                    }
                    TimeCapsuleType.RECEIVED -> {
                        items(timeCapsuleReceived) { timeCapsule ->
                            ReceivedTimeCapsule(timeCapsule, onNavigateToViewTimeCapsule)
                            if (timeCapsule != timeCapsuleSent.last()){
                                HorizontalDivider(thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Padding for spacing from screen edges
                contentAlignment = Alignment.BottomEnd // Align content to bottom-right
            ) {
                FloatingActionButton (
                    onClick = onNavigateToCreateTimeCapsule,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, "Small floating action button.")
                }
            }

            val deleteDialogCapsuleId = (uiState as AllTimeCapsulesUiState.Success).deleteDialogCapsuleId

            if (deleteDialogCapsuleId.isNotEmpty()) {
                ConfirmDelete (
                    stringResource(R.string.confirm_diary_deletion),
                    stringResource(R.string.delete_diary),
                    viewModel::onCloseDeleteTimeCapsuleDialog,
                    viewModel::onDeleteTimeCapsule
                )
            }
        }
        is AllTimeCapsulesUiState.Error -> Text(text = "Error: ${(uiState as AllTimeCapsulesUiState.Error).exception.message}")
    }
}

@Composable
fun TimeCapsuleComposable(modifier: Modifier) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
            contentDescription = null
        )
    }
}

@Composable
fun ScheduledTimeCapsule(timeCapsule: TimeCapsule, onClick: (String, TimeCapsuleType) -> Unit, onOpenDeleteTimeCapsuleDialog: () -> Unit) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(timeCapsule.id, TimeCapsuleType.SCHEDULED) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
        Column(
            modifier = Modifier.weight(2f)
        ){
            Text(
                text = timeCapsule.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                Text(
                    text = (timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count().toString() +
                        if((timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count() == 1){
                            " destinatario"
                        } else {
                            " destinatari"
                        }
                )
                Text(
                    text = "Creata il " + formatter.format(timeCapsule.creationDate.toDate())
                )
                Text(text = "In arrivo il " + formatter.format(timeCapsule.deadline.toDate()))
            }
        }
        IconButton (
            onClick = onOpenDeleteTimeCapsuleDialog,
            colors = IconButtonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.primary
            ),
//                                                modifier = Modifier
//                                                    .align(Alignment.Bottom),
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun SentTimeCapsule(timeCapsule: TimeCapsule, onClick: (String, TimeCapsuleType) -> Unit) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format

    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(timeCapsule.id, TimeCapsuleType.SENT) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = timeCapsule.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                Text(
                    text = (timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count()
                        .toString() +
                            if ((timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count() == 1) {
                                " destinatario"
                            } else {
                                " destinatari"
                            }
                )
                Text(
                    text = "Creata il " + formatter.format(timeCapsule.creationDate.toDate())
                )
                Text(text = "Arrivata il " + formatter.format(timeCapsule.deadline.toDate()))
            }
        }
    }
}

@Composable
fun ReceivedTimeCapsule(timeCapsule: TimeCapsule, onClick: (String, TimeCapsuleType) -> Unit) {
    val formatter = SimpleDateFormat("dd MMMM yyyy") // Define the desired format

    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(timeCapsule.id, TimeCapsuleType.RECEIVED) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "https://wjecfnvsxxnvgheqdnpx.supabase.co/storage/v1/object/sign/time-capsules/letter.png?token=eyJraWQiOiJzdG9yYWdlLXVybC1zaWduaW5nLWtleV8xNTIwYmQ5Yy05ZTUxLTQ5MjMtODRmMy1kNzFiNTRkNTNjZjUiLCJhbGciOiJIUzI1NiJ9.eyJ1cmwiOiJ0aW1lLWNhcHN1bGVzL2xldHRlci5wbmciLCJpYXQiOjE3NTA3NTc1MDQsImV4cCI6MTc4MjI5MzUwNH0.RTnD7Gu7q2mF6MlXhHmZXgn-xN4QJ3CVxUt4xf48s98",
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
        Column(
            modifier = Modifier.weight(2f)
        ) {
            Text(
                text = timeCapsule.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Column {
                Text(
                    text = (timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count()
                        .toString() +
                            if ((timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count() == 1) {
                                " destinatario"
                            } else {
                                " destinatari"
                            }
                )
                Text(
                    text = "Creata il " + formatter.format(timeCapsule.creationDate.toDate())
                )
                Text(text = "Ricevuta il " + formatter.format(timeCapsule.deadline.toDate()))
            }
        }
    }
}
