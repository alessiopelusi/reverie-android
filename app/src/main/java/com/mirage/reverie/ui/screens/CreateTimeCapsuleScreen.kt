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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
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
                EmailForm(viewModel::onUpdateEmailList)
                PhoneNumberForm(viewModel::onUpdatePhoneList)

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

@Composable
fun EmailForm(onUpdateEmailList: (List<String>) -> Unit) {
    var emails by remember { mutableStateOf(listOf("")) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Destinatari Email", style = MaterialTheme.typography.titleMedium)

        emails.forEachIndexed { index, email ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { newValue ->
                        onUpdateEmailList(emails.toMutableList().also { it[index] = newValue })
                    },
                    label = { Text("Email ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (emails.size > 1) {
                    IconButton(onClick = {
                        emails = emails.toMutableList().also { it.removeAt(index) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                    }
                }
            }

            // Messaggio di errore sotto il campo
            if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Text(
                    "Email non valida",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = { emails = emails + "" },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Aggiungi un altro destinatario")
        }
    }
}

@Composable
fun PhoneNumberForm(onUpdatePhoneList: (List<String>) -> Unit) {
    // Ogni voce è una Pair di prefisso e numero
    var phones by remember { mutableStateOf(listOf("")) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Numeri di telefono", style = MaterialTheme.typography.titleMedium)

        phones.forEachIndexed { index, number ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { newValue ->
                        onUpdatePhoneList(phones.toMutableList().also { it[index] to newValue })
                    },
                    label = { Text("Numero") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = number.length < 5 || !number.startsWith("+") || !number.drop(1).all { it.isDigit() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (phones.size > 1) {
                    IconButton (onClick = {
                        phones = phones.toMutableList().also { it.removeAt(index) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Rimuovi")
                    }
                }
            }

            // Mostra messaggi di errore sotto i campi
            if (!number.startsWith("+")) {
                Text(
                    "Numero non valido, aggiungi il prefisso (es. +39)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (number.length < 5 || !number.drop(1).all { it.isDigit() }) {
                Text(
                    "Numero non valido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = { phones = phones + "" },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Aggiungi un altro numero")
        }
    }
}