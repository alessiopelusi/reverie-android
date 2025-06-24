package com.mirage.reverie.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.R
import com.mirage.reverie.ui.components.ContentTextField
import com.mirage.reverie.viewmodel.CreateTimeCapsuleUiState
import com.mirage.reverie.viewmodel.CreateTimeCapsuleViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun CreateTimeCapsuleScreen(
    onComplete: (TimeCapsule) -> Unit,
    viewModel: CreateTimeCapsuleViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when(uiState) {
        is CreateTimeCapsuleUiState.Loading -> CircularProgressIndicator()
        is CreateTimeCapsuleUiState.Idle, is CreateTimeCapsuleUiState.Error -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            val timeCapsule = formState.timeCapsule

            Column (
                modifier = Modifier.padding(0.dp, 20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.create_time_capsule_message),
                )

                EditTitleField(timeCapsule.title, onNewValue = viewModel::onUpdateTitle)
                ContentTextField (timeCapsule.content, onUpdateContent = viewModel::onUpdateContent)
                DatePicker(viewModel::onUpdateDeadline)

                Button (
                    onClick = viewModel::onCreateTimeCapsule
                ) {
                    Text(stringResource(R.string.create))
                }


                if (uiState is CreateTimeCapsuleUiState.Error) {
                    Text(text = (uiState as CreateTimeCapsuleUiState.Error).errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is CreateTimeCapsuleUiState.Success -> {
            val formState by viewModel.formState.collectAsStateWithLifecycle()
            onComplete(formState.timeCapsule)
        }
    }
}

@Composable
fun DatePicker(onUpdateDate: (Timestamp) -> Unit){
    val context = LocalContext.current

    // Stato per la data selezionata
    var selectedDate by remember { mutableStateOf("") }

    // Ottieni data attuale per default
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0) // opzionale: per normalizzare l'ora
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val date = selectedCalendar.time
            val firebaseTimestamp = Timestamp(date)

            onUpdateDate(firebaseTimestamp)

            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val formattedDate = formatter.format(date)

            selectedDate = formattedDate
        },
        year, month, day
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(text = if (selectedDate.isEmpty()) stringResource(R.string.no_date_selected) else "${stringResource(R.string.date)}: $selectedDate")

    Button(onClick = { datePickerDialog.show() }) {
        Text(text = stringResource(R.string.select_date))
    }
}