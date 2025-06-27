package com.mirage.reverie.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.User
import com.mirage.reverie.formatDate
import com.mirage.reverie.ui.components.SingleLineField
import com.mirage.reverie.ui.components.PhoneNumber
import com.mirage.reverie.ui.components.formatPhoneNumber
import java.text.SimpleDateFormat
import java.time.ZoneId
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
            val matchingUsers = formState.matchingUsers
            val partialUsername = formState.partialUsername
            val receivers = formState.userReceivers

            Column (
                modifier = Modifier.padding(0.dp, 20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ){
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(R.string.create_time_capsule_message),
                )

                SingleLineField(timeCapsule.title, formState.titleError, onNewValue = viewModel::onUpdateTitle, stringResource(R.string.title))

                ContentTextField (timeCapsule.content, formState.contentError, viewModel::onUpdateContent, stringResource(R.string.content))

                Spacer(modifier = Modifier.height(8.dp))

                val formattedDate = formatDate(timeCapsule.deadline.toDate())

                Text(text = if (timeCapsule.deadline < Timestamp.now()) stringResource(R.string.no_date_selected) else "${stringResource(R.string.date)}: $formattedDate")

                DatePicker(timeCapsule.deadline, viewModel::onUpdateDeadline)

                PhonesList(timeCapsule.phones, viewModel::onRemovePhoneNumber)
                EmailsList(timeCapsule.emails, viewModel::onRemoveEmail)
                ReceiversList(receivers, viewModel::onRemoveUser)

                Row{
                    PhoneNumber(formState.phoneNumber, formState.phoneNumberError, viewModel::onUpdatePhoneNumber)
                    Button (
                        onClick = viewModel::onAddPhoneNumber
                    ) {
                        Text(stringResource(R.string.create))
                    }
                }

                Row {
                    SingleLineField(formState.email, formState.emailError, viewModel::onUpdateEmail, stringResource(R.string.email))
                    Button (
                        onClick = viewModel::onAddEmail
                    ) {
                        Text(stringResource(R.string.create))
                    }
                }

                SingleLineField(partialUsername, viewModel::onUpdatePartialUsername, stringResource(R.string.username))
                SelectUserDropDownMenu(matchingUsers, onSelectedUser = viewModel::onAddUser)

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
fun DatePicker(
    selectedDate: Timestamp,
    onUpdateDate: (Timestamp) -> Unit
){
    val lastYear = selectedDate.toInstant().atZone(ZoneId.systemDefault()).year
    val lastMonth = selectedDate.toInstant().atZone(ZoneId.systemDefault()).monthValue-1 // 1-index month
    val lastDay = selectedDate.toInstant().atZone(ZoneId.systemDefault()).dayOfMonth

    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
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
        },
        lastYear, lastMonth, lastDay
    )

    // Disable dates before or equal to today
    val tomorrow = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
    }
    datePickerDialog.datePicker.minDate = tomorrow.timeInMillis

    Button(onClick = { datePickerDialog.show() }) {
        Text(text = stringResource(R.string.select_date))
    }
}

@Composable
fun PhonesList(
    phones: List<String>,
    onRemovePhone: (String) -> Unit
) {
    Column {
        phones.forEach { phone ->
            Row {
                Text(formatPhoneNumber(phone))
                IconButton (
                    onClick = { onRemovePhone(phone) },
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
    }
}

@Composable
fun EmailsList(
    emails: List<String>,
    onRemoveEmail: (String) -> Unit
) {
    Column {
        emails.forEach { email ->
            Row {
                Text(email)
                IconButton (
                    onClick = { onRemoveEmail(email) },
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
    }
}

@Composable
fun ReceiversList(
    receivers: List<User>,
    onRemoveReceiver: (User) -> Unit
) {
    Column {
        receivers.forEach { user ->
            Row {
                Text(user.username)
                IconButton (
                    onClick = { onRemoveReceiver(user) },
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
    }
}

@Composable
fun SelectUserDropDownMenu(
    users: List<User>,
    onSelectedUser: (User) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp) // Limit the height of the results
            .border(1.dp, Color.Gray)
    ) {
        LazyColumn {
            items(users) { user ->
                Text(
                    text = user.username,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectedUser(user) }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
