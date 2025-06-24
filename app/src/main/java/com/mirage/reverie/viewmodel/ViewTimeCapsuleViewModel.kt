package com.mirage.reverie.viewmodel

import com.mirage.reverie.data.model.TimeCapsule
import com.mirage.reverie.data.repository.TimeCapsuleRepository
import com.mirage.reverie.navigation.ViewTimeCapsuleRoute

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ViewTimeCapsuleFormState(
    val timeCapsule: TimeCapsule = TimeCapsule()
)


sealed class ViewTimeCapsuleUiState {
    data object Loading : ViewTimeCapsuleUiState()
    data object Idle: ViewTimeCapsuleUiState()
    data object Success : ViewTimeCapsuleUiState()
    data class Error(val errorMessage: String) : ViewTimeCapsuleUiState()
}

@HiltViewModel
class ViewTimeCapsuleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TimeCapsuleRepository
) : ViewModel() {
    private val timeCapsuleId = savedStateHandle.toRoute<ViewTimeCapsuleRoute>().timeCapsuleId

    private val _uiState = MutableStateFlow<ViewTimeCapsuleUiState>(ViewTimeCapsuleUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(ViewTimeCapsuleFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    private fun onStart() {
        viewModelScope.launch {
            val timeCapsule = repository.getTimeCapsule(timeCapsuleId)

            _uiState.update { ViewTimeCapsuleUiState.Idle }
            _formState.update { ViewTimeCapsuleFormState(timeCapsule) }
        }
    }


    fun onUpdateTimeCapsule() {
        _uiState.update { ViewTimeCapsuleUiState.Idle }

        val state = formState.value
        if (state.timeCapsule.content.isBlank()) {
            _uiState.update { ViewTimeCapsuleUiState.Error("Il contenuto è obbligatorio") }
        }

        viewModelScope.launch {
            try {
                //repository.updatePage(state.page)
                _uiState.update { ViewTimeCapsuleUiState.Success }
            } catch (exception: Exception) {
                _uiState.update { ViewTimeCapsuleUiState.Error(exception.message.toString()) } // Gestisci errori
            }
        }
    }

    // Handle business logic
    fun onUpdateContent(newContent: String) {
        val currentState = uiState.value
        if (currentState is ViewTimeCapsuleUiState.Loading) return

/*
        _formState.update { state ->
            val updatedPage = state.page.copy(content = newContent)
            state.copy(page = updatedPage)
        }
*/
    }
}
