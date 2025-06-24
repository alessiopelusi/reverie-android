package com.mirage.reverie.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.viewmodel.ViewTimeCapsuleState
import com.mirage.reverie.viewmodel.ViewTimeCapsuleViewModel

@Composable
fun ViewTimeCapsuleScreen(
    viewModel: ViewTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is ViewTimeCapsuleState.Loading -> CircularProgressIndicator()
        is ViewTimeCapsuleState.Success -> {
            val timeCapsule = (uiState as ViewTimeCapsuleState.Success).timeCapsule

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                //verticalArrangement = Arrangement.Center
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.title
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.userId
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.creationDate.toString()
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.deadline.toString()
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.emails.toString()
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.phones.toString()
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.receivers.toString()
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = timeCapsule.content
                )
            }
        }
        is ViewTimeCapsuleState.Error -> Text(text = "Error: ${(uiState as ViewTimeCapsuleState.Error).exception.message}")
    }
}
