package com.mirage.reverie.viewmodel

import android.content.Context
import android.telephony.PhoneNumberUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.R
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

data class CreateTimeCapsuleFormState(
    val timeCapsule: TimeCapsule = TimeCapsule(),
    val titleError: String = "",
    val contentError: String = "",
    val deadlineError: String = "",
    val phoneNumber: String = "",
    val phoneNumberError: String = "",
    val email: String = "",
    val emailError: String = "",
    val partialUsername: String = "",
    val matchingUsers: List<User> = listOf(),
    val userReceivers: List<User> = listOf()
) {
    val allReceivers: List<String>
        get() = timeCapsule.phones + timeCapsule.emails + timeCapsule.receiversIds
}

sealed class CreateTimeCapsuleUiState {
    data object Loading : CreateTimeCapsuleUiState()
    data object Idle : CreateTimeCapsuleUiState()
    data object Success : CreateTimeCapsuleUiState()
    data class Error(val errorMessage: String) : CreateTimeCapsuleUiState()
}

// used both for edit and create
@HiltViewModel
class CreateTimeCapsuleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TimeCapsuleRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateTimeCapsuleUiState>(CreateTimeCapsuleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CreateTimeCapsuleFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun onStart() {
        var timeCapsule = TimeCapsule()
        auth.uid?.let { timeCapsule = TimeCapsule(userId = it) }

        _uiState.update { CreateTimeCapsuleUiState.Idle }
        _formState.update { CreateTimeCapsuleFormState(timeCapsule) }
    }

    private fun validateTitle(title: String): String {
        return if (title.isBlank()) {
            context.getString(R.string.title_mandatory)
        } else {
            ""
        }
    }

    private fun validateContent(content: String): String {
        return if (content.isBlank()) {
            context.getString(R.string.content_mandatory)
        } else {
            ""
        }
    }

    private fun validateDeadline(date: Timestamp): String {
        return if (date < Timestamp.now()) {
            context.getString(R.string.deadline_mandatory)
        } else {
            ""
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): String {
        return when {
            phoneNumber.isBlank() -> ""
            phoneNumber in formState.value.timeCapsule.phones -> context.getString(R.string.phone_number_already_selected)
            !PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber) -> context.getString(R.string.phone_number_not_valid)
            else -> ""
        }
    }

    private fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> ""
            email in formState.value.timeCapsule.emails -> context.getString(R.string.email_already_selected)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> context.getString(R.string.email_not_valid)
            else -> ""
        }
    }

    fun onUpdateTitle(newTitle: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        val error = validateTitle(newTitle)

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(title = newTitle)
            state.copy(
                timeCapsule = updatedTimeCapsule,
                titleError = error
            )
        }
    }

    fun onUpdateContent(newContent: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        val error = validateContent(newContent)

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(content = newContent)
            state.copy(
                timeCapsule = updatedTimeCapsule,
                contentError = error
            )
        }
    }

    fun onUpdateDeadline(newDeadline: Timestamp) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        // don't update if date earlier than now
        if (newDeadline < Timestamp.now()) return

        val error = validateDeadline(newDeadline)

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(deadline = newDeadline)
            state.copy(
                timeCapsule = updatedTimeCapsule,
                deadlineError = error
            )
        }
    }

    fun onUpdatePhoneNumber(newPhoneNumber: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        // regex that remover every non-number character except for + on the first position
        val regex = Regex("""(?<=^)[^+\d]|(?!^)[^0-9]""")
        val strippedPhoneNumber = regex.replace(newPhoneNumber.trim(), "")

        val error = validatePhoneNumber(strippedPhoneNumber)

        _formState.update { state ->
            state.copy(
                phoneNumber = strippedPhoneNumber,
                phoneNumberError = error
            )
        }
    }

    fun onUpdateEmail(newEmail: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        val error = validateEmail(newEmail)

        _formState.update { state ->
            state.copy(
                email = newEmail,
                emailError = error
            )
        }
    }

    fun onUpdateEmailList(newEmailList: List<String>) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(emails = newEmailList)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdatePhoneList(newPhoneList: List<String>) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(phones = newPhoneList)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdateReceiverIds(newReceiverIds: List<String>) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(receiversIds = newReceiverIds)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onAddUser(user: User) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            // already in
            if (user in state.userReceivers) return

            val updatedReceiverIds = state.timeCapsule.receiversIds.toMutableList()
            updatedReceiverIds.add(user.id)

            val updatedTimeCapsule = state.timeCapsule.copy(receiversIds = updatedReceiverIds)

            val updatedReceivers = state.userReceivers.toMutableList()
            updatedReceivers.add(user)

            state.copy(
                timeCapsule = updatedTimeCapsule,
                userReceivers = updatedReceivers
            )
        }
    }

    fun onRemoveUser(user: User) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedReceiverIds = state.timeCapsule.receiversIds.toMutableList()
            updatedReceiverIds.remove(user.id)

            val updatedTimeCapsule = state.timeCapsule.copy(receiversIds = updatedReceiverIds)

            val updatedReceivers = state.userReceivers.toMutableList()
            updatedReceivers.remove(user)

            state.copy(
                timeCapsule = updatedTimeCapsule,
                userReceivers = updatedReceivers
            )
        }
    }

    fun onAddPhoneNumber() {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val phoneNumber = state.phoneNumber

            // number not valid
            if (validatePhoneNumber(phoneNumber).isNotEmpty()) return

            // already in
            if (phoneNumber in state.timeCapsule.phones) return

            val updatedPhones = state.timeCapsule.phones.toMutableList()
            updatedPhones.add(phoneNumber)

            val updatedTimeCapsule = state.timeCapsule.copy(phones = updatedPhones)

            state.copy(
                timeCapsule = updatedTimeCapsule,
            )
        }
    }

    fun onRemovePhoneNumber(phoneNumber: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedPhones = state.timeCapsule.phones.toMutableList()
            updatedPhones.remove(phoneNumber)

            val updatedTimeCapsule = state.timeCapsule.copy(phones = updatedPhones)

            state.copy(
                timeCapsule = updatedTimeCapsule,
            )
        }
    }

    fun onAddEmail() {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val email = state.email

            // number not valid
            if (validateEmail(email).isNotEmpty()) return

            // already in
            if (email in state.timeCapsule.emails) return

            val updatedEmails = state.timeCapsule.emails.toMutableList()
            updatedEmails.add(email)

            val updatedTimeCapsule = state.timeCapsule.copy(emails = updatedEmails)

            state.copy(
                timeCapsule = updatedTimeCapsule,
            )
        }
    }

    fun onRemoveEmail(email: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedEmails = state.timeCapsule.emails.toMutableList()
            updatedEmails.remove(email)

            val updatedTimeCapsule = state.timeCapsule.copy(emails = updatedEmails)

            state.copy(
                timeCapsule = updatedTimeCapsule,
            )
        }
    }

/*
    fun onRemoveReceiver(receiver: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedEmails = state.timeCapsule.emails.toMutableList()
            updatedEmails.remove(receiver)

            val updatedPhones = state.timeCapsule.phones.toMutableList()
            updatedPhones.remove(receiver)

            val updatedReceiverIds = state.timeCapsule.receiversIds.toMutableList()
            updatedReceiverIds.remove(receiver)

            val updatedTimeCapsule = state.timeCapsule.copy(
                phones = updatedPhones,
                emails = updatedEmails,
                receiversIds = updatedReceiverIds
            )

            state.copy(
                timeCapsule = updatedTimeCapsule,
            )
        }
    }
*/

    private var matchingUsersJob: Job? = null

    fun onUpdatePartialUsername(newPartialUsername: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            state.copy(
                partialUsername = newPartialUsername,
                matchingUsers = listOf()
            )
        }

        // Cancel any ongoing username check
        matchingUsersJob?.cancel()

        // minimum lenght is 1 for search
        if (newPartialUsername.isEmpty()) return

        // Start a new coroutine for validation
        matchingUsersJob = viewModelScope.launch {
            val matchingUsers = userRepository.getUsersMatchingPartialUsername(newPartialUsername)

            _formState.update { state ->
                state.copy(
                    matchingUsers = matchingUsers
                )
            }
        }
    }

    fun onCreateTimeCapsule() {
        _uiState.update { CreateTimeCapsuleUiState.Idle }

        val state = formState.value
        if (state.timeCapsule.title.isBlank()) {
            _uiState.update { CreateTimeCapsuleUiState.Error(context.getString(R.string.title_mandatory)) }
        }
        if (state.timeCapsule.content.isBlank()) {
            _uiState.update { CreateTimeCapsuleUiState.Error(context.getString(R.string.content_mandatory)) }
        }
        if (state.timeCapsule.deadline < Timestamp.now()) {
            _uiState.update { CreateTimeCapsuleUiState.Error(context.getString(R.string.select_a_date)) }
        }

        if (state.timeCapsule.emails.isEmpty() && state.timeCapsule.phones.isEmpty() && state.timeCapsule.receiversIds.isEmpty()) {
            _uiState.update { CreateTimeCapsuleUiState.Error(context.getString(R.string.receiver_mandatory)) }
        }

        // if uiState is error, we save the error string and return
        if (uiState.value is CreateTimeCapsuleUiState.Error) {
            return
        }

        viewModelScope.launch {
            try {
                _formState.update {
                    state.copy(timeCapsule = repository.saveTimeCapsule(state.timeCapsule))
                }
                _uiState.value = CreateTimeCapsuleUiState.Success
            } catch (exception: Exception) {
                _uiState.value = CreateTimeCapsuleUiState.Error(exception.message.toString()) // Gestisci errori
            }
        }
    }
}