package com.mirage.reverie.ui.screens

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
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
import java.text.SimpleDateFormat

@Composable
fun ViewTimeCapsuleScreen(
    viewModel: ViewTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is ViewTimeCapsuleState.Loading -> CircularProgressIndicator()
        is ViewTimeCapsuleState.Success -> {
            val timeCapsule = (uiState as ViewTimeCapsuleState.Success).timeCapsule
            val timeCapsuleType = (uiState as ViewTimeCapsuleState.Success).timeCapsuleType
            val scrollState = rememberScrollState()

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
                        text = "Contenuto della lettera:",
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
                        text = "Contenuto non disponibile.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Mittente:",
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
                        text = if ((timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds).count() == 1) {
                            "Destinatario:"
                        } else {
                            "Destinatari:"
                        },
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                    )
                    for (destinatario in (timeCapsule.emails + timeCapsule.phones + timeCapsule.receiversIds)){
                        Text(
                            text = destinatario
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
                            text = "Data di Creazione:",
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
                            text = "Data di Arrivo:",
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
