package com.mirage.reverie.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.R
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.navigation.EditDiaryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

data class EditDiaryFormState(
    val diary: Diary = Diary(),
    val allCoversMap: Map<String, DiaryCover> = mapOf(),
    val selectedCover: String = diary.coverId,
    val titleError: String = ""
)

sealed class EditDiaryUiState {
    data object Loading : EditDiaryUiState()
    data object Idle : EditDiaryUiState()
    data object Success : EditDiaryUiState()
    data class Error(val errorMessage: String) : EditDiaryUiState()
}

// used both for edit and create
@HiltViewModel
class EditDiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository,
    private val auth: FirebaseAuth,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditDiaryUiState>(EditDiaryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EditDiaryFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    // load diary
    @OptIn(ExperimentalSerializationApi::class)
    private fun onStart() {
        viewModelScope.launch {
            var diary = Diary()

            try {
                val diaryId = savedStateHandle.toRoute<EditDiaryRoute>().diaryId
                diary = repository.getDiary(diaryId)
            } catch (e: kotlinx.serialization.MissingFieldException) {
                auth.uid?.let {
                    diary = Diary(
                        uid = it,
                        title = context.getString(R.string.new_diary),
                        description = "",
                        coverId = "MVl66divWlJIIGvaBFbw",
                    )
                }
            }

            val allCovers = repository.getAllDiaryCovers()
            val allCoversMap = allCovers.associateBy { cover -> cover.id }

            _formState.update { state ->
                state.copy(diary = diary, allCoversMap = allCoversMap)
            }

            _uiState.value = EditDiaryUiState.Idle
        }
    }

    fun onUpdateDiary() {
        _uiState.update { EditDiaryUiState.Idle }

        val titleError = validateTitle(formState.value.diary.title)
        if (titleError.isNotBlank()) {
            _formState.update { state ->
                state.copy(
                    titleError = titleError
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val state = formState.value
                if (state.diary.id != "") {
                    repository.updateDiary(state.diary)
                } else {
                    _formState.update {
                        state.copy(diary = repository.saveDiary(state.diary))
                    }
                }
                _uiState.value = EditDiaryUiState.Success
            } catch (exception: Exception) {
                _uiState.value = EditDiaryUiState.Error(exception.message.toString()) // Gestisci errori
            }
        }
    }


    private fun validateTitle(title: String): String {
        return if (title.isBlank()) {
            context.getString(R.string.title_mandatory)
        } else {
            ""
        }
    }


    fun onUpdateTitle(newTitle: String) {
        val currentState = uiState.value
        if (currentState is EditDiaryUiState.Loading) return

        val strippedTitle = newTitle.trim()
        if (strippedTitle == formState.value.diary.title) return

        val error = validateTitle(strippedTitle)

        _formState.update { state ->
            val updatedDiary = state.diary.copy(title = strippedTitle)
            state.copy(
                diary = updatedDiary,
                titleError = error
            )
        }
    }

    fun onUpdateCover(newCoverId: String) {
        val currentState = uiState.value
        if (currentState is EditDiaryUiState.Loading) return

        _formState.update { state ->
            val updatedDiary = state.diary.copy(coverId = newCoverId)
            state.copy(diary = updatedDiary)
        }
    }
}
