package com.mirage.reverie.ui.screens


import androidx.compose.foundation.Image
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

import com.mirage.reverie.viewmodel.AllTimeCapsulesUiState
import com.mirage.reverie.viewmodel.AllTimeCapsulesViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mirage.reverie.R
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.formatDate
import com.mirage.reverie.ui.components.ButtonBar
import com.mirage.reverie.ui.components.ConfirmDelete
import com.mirage.reverie.viewmodel.TimeCapsuleType


@Composable
fun AllTimeCapsulesScreen(
    onNavigateToCreateTimeCapsule: () -> Unit,
    onNavigateToViewTimeCapsule: (String, TimeCapsuleType) -> Unit,
    newTimeCapsule: TimeCapsule? = null,
    viewModel: AllTimeCapsulesViewModel = hiltViewModel()
) {
    // add new time capsule sent from CreateTimeCapsuleRoute
    viewModel.addNewTimeCapsule(newTimeCapsule)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is AllTimeCapsulesUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                            TimeCapsuleComposable()
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                text = stringResource(R.string.letter_for_the_future)
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = stringResource(R.string.letter_for_the_future_description)
                            )
                        }
                    }
                }
                item {
                    ButtonBar(buttonState, buttonElements, viewModel::onButtonStateUpdate){ item ->
                        when(item) {
                            TimeCapsuleType.SCHEDULED -> stringResource(R.string.scheduled)
                            TimeCapsuleType.SENT -> stringResource(R.string.sent)
                            else -> stringResource(R.string.received)
                        }
                    }
                }
                when(buttonState) {
                    TimeCapsuleType.SCHEDULED -> {
                        items(timeCapsuleScheduled) { timeCapsule ->
                            TimeCapsule(timeCapsule, TimeCapsuleType.SCHEDULED, onNavigateToViewTimeCapsule) {
                                viewModel.onOpenDeleteTimeCapsuleDialog(timeCapsule.id)
                            }
                            if (timeCapsule != timeCapsuleSent.last()){
                                HorizontalDivider(thickness = 1.dp)
                            }
                        }
                    }
                    TimeCapsuleType.SENT -> {
                        items(timeCapsuleSent) { timeCapsule ->
                            TimeCapsule(timeCapsule, TimeCapsuleType.SENT, onNavigateToViewTimeCapsule)
                            if (timeCapsule != timeCapsuleSent.last()){
                                HorizontalDivider(thickness = 1.dp)
                            }
                        }
                    }
                    TimeCapsuleType.RECEIVED -> {
                        items(timeCapsuleReceived) { timeCapsule ->
                            TimeCapsule(timeCapsule, TimeCapsuleType.RECEIVED, onNavigateToViewTimeCapsule)
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
            ) {
                FloatingActionButton (
                    onClick = onNavigateToCreateTimeCapsule,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Align FAB to the top-right
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.create_time_capsule))
                }
            }

            val deleteDialogCapsuleId = (uiState as AllTimeCapsulesUiState.Success).deleteDialogCapsuleId

            if (deleteDialogCapsuleId.isNotEmpty()) {
                ConfirmDelete (
                    stringResource(R.string.confirm_capsule_deletion),
                    stringResource(R.string.delete_capsule),
                    viewModel::onCloseDeleteTimeCapsuleDialog,
                    viewModel::onDeleteTimeCapsule
                )
            }
        }
        is AllTimeCapsulesUiState.Error -> Text(text = "Error: ${(uiState as AllTimeCapsulesUiState.Error).exception.message}")
    }
}

@Composable
fun TimeCapsuleComposable() {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.letter),
            contentDescription = stringResource(R.string.letter_for_the_future),
        )
    }
}

@Composable
fun TimeCapsule(timeCapsule: TimeCapsule, timeCapsuleType: TimeCapsuleType, onClick: (String, TimeCapsuleType) -> Unit, onOpenDeleteTimeCapsuleDialog: () -> Unit = {}) {
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
            Image(
                painter = painterResource(id = R.drawable.letter),
                contentDescription = stringResource(R.string.letter_for_the_future),
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
                                " " + stringResource(R.string.receiver)
                            } else {
                                " " + stringResource(R.string.receivers)
                            }
                )
                Text(
                    text = stringResource(R.string.created_on) + " " + formatDate(timeCapsule.creationDate.toDate())
                )
                Text(text = stringResource(if (timeCapsuleType == TimeCapsuleType.SCHEDULED) R.string.arriving_on else R.string.arrived_on)
                        + " " + formatDate(timeCapsule.deadline.toDate()))
            }
        }
        if (timeCapsuleType == TimeCapsuleType.SCHEDULED) {
            IconButton (
                onClick = onOpenDeleteTimeCapsuleDialog,
                colors = IconButtonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

