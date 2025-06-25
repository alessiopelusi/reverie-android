package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.R
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.navigation.CreateTimeCapsuleRoute
import com.mirage.reverie.navigation.EditDiaryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val dateError: String = "",
)

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
//        if (state.timeCapsule.emails.isEmpty() && state.timeCapsule.phones.isEmpty()) {
//            _uiState.update { CreateTimeCapsuleUiState.Error("Il contenuto è obbligatorio") }
//        }

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

    // Handle business logic
    fun onUpdateTitle(newTitle: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(title = newTitle)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdateContent(newContent: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(content = newContent)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdateDeadline(newDeadline: Timestamp){
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(deadline = newDeadline)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdateEmailList(newEmailList: List<String>){
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(emails = newEmailList)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }

    fun onUpdatePhoneList(newPhoneList: List<String>){
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

        _formState.update { state ->
            val updatedTimeCapsule = state.timeCapsule.copy(phones = newPhoneList)
            state.copy(timeCapsule = updatedTimeCapsule)
        }
    }
}