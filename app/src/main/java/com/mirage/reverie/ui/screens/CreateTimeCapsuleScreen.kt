package com.mirage.reverie.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.User
import com.mirage.reverie.formatDate
import com.mirage.reverie.ui.components.SingleLineField
import com.mirage.reverie.ui.components.PhoneNumber
import com.mirage.reverie.ui.components.formatPhoneNumber
import java.time.ZoneId
import java.util.Calendar


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

                ContentTextField(timeCapsule.content, formState.contentError, viewModel::onUpdateContent, stringResource(R.string.content))

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ){
                    DatePicker(timeCapsule.deadline, viewModel::onUpdateDeadline)
                    Text(
                        text =
                            if (timeCapsule.deadline < Timestamp.now()) stringResource(R.string.no_date_selected)
                            else "${stringResource(R.string.date)}: ${formatDate(timeCapsule.deadline.toDate())}"
                    )
                }

                SingleLineField(partialUsername, viewModel::onUpdatePartialUsername, stringResource(R.string.username))
                if (matchingUsers.isNotEmpty()) SelectUserDropDownMenu(matchingUsers, onSelectedUser = viewModel::onAddUser)

                PhoneNumber(
                    phoneNumber =  formState.phoneNumber,
                    errorMessage = formState.phoneNumberError,
                    onUpdatePhoneNumber = viewModel::onUpdatePhoneNumber,
                    trailingIcon = { AddIconButton(viewModel::onAddPhoneNumber) })

                SingleLineField(
                    value = formState.email,
                    errorMessage = formState.emailError,
                    onNewValue = viewModel::onUpdateEmail,
                    label = stringResource(R.string.email),
                    trailingIcon = { AddIconButton(viewModel::onAddEmail) }
                )


                Text(
                    text = stringResource(R.string.receivers) + ":",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.width(280.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .width(280.dp)
                        .heightIn(min = 80.dp, max = 193.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(5.dp)),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    items(timeCapsule.phones) { phone ->
                        ReceiverElement(formatPhoneNumber(phone)) { viewModel.onRemovePhoneNumber(phone) }
                    }
                    items(timeCapsule.emails) { email ->
                        ReceiverElement(email) { viewModel.onRemoveEmail(email) }
                    }
                    items(receivers) { receiver ->
                        ReceiverElement(receiver.username) { viewModel.onRemoveUser(receiver) }
                    }
                }

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
fun ReceiverElement(
    text: String,
    onDelete: () -> Unit
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .offset(x = (-4).dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete)
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete),
            )
        }
        Text(
            text = text,
        )
    }
}

@Composable
fun SelectUserDropDownMenu(
    users: List<User>,
    onSelectedUser: (User) -> Unit
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .heightIn(max = 140.dp) // Limit the height of the results (3.5 elements)
            .offset(0.dp, (-20).dp)
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(5.dp))
    ) {
        LazyColumn (
            contentPadding = PaddingValues(vertical = 2.dp),
        ){
            items(users) { user ->
                Text(
                    text = user.username,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .clickable { onSelectedUser(user) }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AddIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = stringResource(R.string.add),
            tint = MaterialTheme.colorScheme.primaryContainer
        )
    }
}
