package com.mirage.reverie.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.mirage.reverie.formatDate
import com.mirage.reverie.viewmodel.TimeCapsuleType
import com.mirage.reverie.viewmodel.ViewTimeCapsuleState
import com.mirage.reverie.viewmodel.ViewTimeCapsuleViewModel

@Composable
fun ViewTimeCapsuleScreen(
    onViewProfile: (String) -> Unit,
    viewModel: ViewTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is ViewTimeCapsuleState.Loading -> CircularProgressIndicator()
        is ViewTimeCapsuleState.Success -> {
            val timeCapsule = (uiState as ViewTimeCapsuleState.Success).timeCapsule
            val timeCapsuleType = (uiState as ViewTimeCapsuleState.Success).timeCapsuleType
            val scrollState = rememberScrollState()
            val receiversUsername = (uiState as ViewTimeCapsuleState.Success).receiversUsername

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 25.dp, vertical = 50.dp),
            ){

                Text(
                    text = timeCapsule.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(50.dp))

                if (timeCapsuleType != TimeCapsuleType.SCHEDULED) {
                    Text(
                        text = stringResource(R.string.letter_content) + ":",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = timeCapsule.content,
                        textAlign = TextAlign.Justify
                    )
                } else {
                    Text(
                        text = stringResource(R.string.content_not_available) + ".",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = stringResource(R.string.sender) + ":",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                )
                Text(
                    text = timeCapsule.userId
                )

                if (timeCapsuleType != TimeCapsuleType.RECEIVED) {
                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        text = if ((timeCapsule.phones + timeCapsule.emails + timeCapsule.receiversIds).count() == 1) {
                            stringResource(R.string.receiver) + ":"
                        } else {
                            stringResource(R.string.receivers) + ":"
                        },
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                    )
                    for (receiver in (timeCapsule.phones + timeCapsule.emails)){
                        Text(
                            text = receiver
                        )
                    }
                    for (receiver in receiversUsername) {
                        Text(
                            text = receiver.username,
                            modifier = Modifier.clickable { onViewProfile(receiver.uid) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.creation_date) + ":",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = formatDate(timeCapsule.creationDate.toDate())
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.arrival_date) + ":",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                        )
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = formatDate(timeCapsule.deadline.toDate())

                        )
                    }
                }
            }
        }
        is ViewTimeCapsuleState.Error -> Text(text = "${stringResource(R.string.error)}: ${(uiState as ViewTimeCapsuleState.Error).exception.message}")
    }
}
