package com.mirage.reverie.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.navigation.EditDiaryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditDiaryFormState(
    val diary: Diary = Diary()
)

sealed class EditDiaryUiState {
    data object Loading : EditDiaryUiState()
    data object Idle : EditDiaryUiState()
    data object Success : EditDiaryUiState()
    data class Error(val errorMessage: String) : EditDiaryUiState()
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class EditDiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository,
) : ViewModel() {
    private val diaryId = savedStateHandle.toRoute<EditDiaryRoute>().diaryId

    private val _uiState = MutableStateFlow<EditDiaryUiState>(EditDiaryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EditDiaryFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    // load diary
    private fun onStart() {
        viewModelScope.launch {
            val diary = repository.getDiary(diaryId)

            _formState.update { state ->
                state.copy(diary = diary)
            }

            _uiState.value = EditDiaryUiState.Idle
        }
    }

    fun onUpdateDiary() {
        _uiState.update { EditDiaryUiState.Idle }

        val state = formState.value
        if (state.diary.title.isBlank()) {
            _uiState.update { EditDiaryUiState.Error("Il titolo è obbligatorio") }
        }

        viewModelScope.launch {
            try {
                repository.updateDiary(state.diary)
                _uiState.value = EditDiaryUiState.Success
            } catch (exception: Exception) {
                _uiState.value = EditDiaryUiState.Error(exception.message.toString()) // Gestisci errori
            }
        }
    }

    // Handle business logic
    fun onUpdateTitle(newTitle: String) {
        val currentState = uiState.value
        if (currentState is EditDiaryUiState.Loading) return

        _formState.update { state ->
            val updatedDiary = state.diary.copy(title = newTitle)
            state.copy(diary = updatedDiary)
        }
    }
}
