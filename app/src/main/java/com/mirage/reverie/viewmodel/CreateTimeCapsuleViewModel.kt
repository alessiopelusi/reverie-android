package com.mirage.reverie.viewmodel

import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.navigation.ViewTimeCapsuleRoute

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.navigation.CreateTimeCapsuleRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class CreateTimeCapsuleFormState(
    val timeCapsule: TimeCapsule = TimeCapsule()
)


sealed class CreateTimeCapsuleUiState {
    data object Loading : CreateTimeCapsuleUiState()
    data object Idle: CreateTimeCapsuleUiState()
    data object Success : CreateTimeCapsuleUiState()
    data class Error(val errorMessage: String) : CreateTimeCapsuleUiState()
}

@HiltViewModel
class CreateTimeCapsuleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth,
    private val repository: TimeCapsuleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreateTimeCapsuleUiState>(CreateTimeCapsuleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ViewTimeCapsuleFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    private fun onStart() {
        var timeCapsule = TimeCapsule()
        auth.uid?.let { timeCapsule = TimeCapsule(userId = it) }

        _uiState.update { CreateTimeCapsuleUiState.Idle }
        _formState.update { ViewTimeCapsuleFormState(timeCapsule) }
    }


    fun onUpdateTimeCapsule() {
        _uiState.update { CreateTimeCapsuleUiState.Idle }

        val state = formState.value
        if (state.timeCapsule.content.isBlank()) {
            _uiState.update { CreateTimeCapsuleUiState.Error("Il contenuto è obbligatorio") }
        }

        viewModelScope.launch {
            try {
                //repository.updatePage(state.page)
                _uiState.update { CreateTimeCapsuleUiState.Success }
            } catch (exception: Exception) {
                _uiState.update { CreateTimeCapsuleUiState.Error(exception.message.toString()) } // Gestisci errori
            }
        }
    }

    // Handle business logic
    fun onUpdateContent(newContent: String) {
        val currentState = uiState.value
        if (currentState is CreateTimeCapsuleUiState.Loading) return

/*
        _formState.update { state ->
            val updatedPage = state.page.copy(content = newContent)
            state.copy(page = updatedPage)
        }
*/
    }
}
