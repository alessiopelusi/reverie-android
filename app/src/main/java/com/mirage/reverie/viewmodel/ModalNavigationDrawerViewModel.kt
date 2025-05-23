package com.mirage.reverie.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.data.model.AllDiaries
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.User
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.navigation.ProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ModalNavigationDrawerUiState {
    data object Loading : ModalNavigationDrawerUiState()
    data class Success(
        val diaries: List<Diary>
    ) : ModalNavigationDrawerUiState()
    data class Error(val exception: Throwable) : ModalNavigationDrawerUiState()
}

@HiltViewModel
class ModalNavigationDrawerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val diaryRepository: DiaryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModalNavigationDrawerUiState>(ModalNavigationDrawerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onOpen()
    }

    private fun onOpen() {
        auth.uid?.let { userId ->
            viewModelScope.launch {
                _uiState.value = ModalNavigationDrawerUiState.Loading
                val diaries = diaryRepository.getUserDiaries(userId)
                _uiState.value = ModalNavigationDrawerUiState.Success(diaries)
            }
        }
    }

    fun onCreateDiary(onSuccess: (Diary) -> Unit) {
        auth.uid?.let { userId ->
            viewModelScope.launch {
                runCatching {
                    val diary = Diary(
                        userId = userId,
                        title = "Nuovo Diario",
                        description = "",
                        coverId = "MVl66divWlJIIGvaBFbw",
                        )
                    diaryRepository.saveDiary(diary)
                }.onSuccess { newDiary ->
                    onSuccess(newDiary)
                }.onFailure { e ->
                    _uiState.value = ModalNavigationDrawerUiState.Error(e)
                }
            }
        }
    }
    fun onDeleteDiary(diaryId: String, onSuccess: () -> Unit = {}){
        viewModelScope.launch {
            runCatching {
                diaryRepository.deleteDiary(diaryId)
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                _uiState.value = ModalNavigationDrawerUiState.Error(e)
            }
        }
    }
}