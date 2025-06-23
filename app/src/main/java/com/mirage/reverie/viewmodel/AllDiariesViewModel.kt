package com.mirage.reverie.viewmodel

import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mirage.reverie.data.model.AllDiaries
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


enum class ButtonState {
    TEXTS, IMAGES, VIDEOS // puoi aggiungere altre sezioni
}

sealed class AllDiariesUiState {
    data object Loading : AllDiariesUiState()
    data class Success(
        val allDiaries: AllDiaries,
        val diariesMap: Map<String, Diary>,
        val diaryCoversMap: Map<String, DiaryCover>,
        val diaryPhotosMap: Map<String, List<DiaryImage>>,
        val pagerState: PagerState = PagerState(
            // endlessPagerMultiplier = 1000
            // endlessPagerMultiplier/2 = 500
            // offset = 1
            pageCount = {if (allDiaries.diaryIds.size > 1) allDiaries.diaryIds.size*1000 else 1},
            currentPage = if (allDiaries.diaryIds.size > 1) allDiaries.diaryIds.size*500 + allDiaries.diaryIds.size/2 else 0
        ),
        val buttonState: ButtonState = ButtonState.IMAGES
    ) : AllDiariesUiState() {
        val diaries: List<Diary>
            get() = allDiaries.diaryIds.map { diaryId -> diariesMap.getValue(diaryId) }

        val currentPage: Int
            get() = pagerState.currentPage % allDiaries.diaryIds.size

        val buttonElements: List<ButtonState>
            get() = ButtonState.entries
    }
    data class Error(val exception: Throwable) : AllDiariesUiState()
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class AllDiariesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    // Expose screen UI state
    private val _uiState = MutableStateFlow<AllDiariesUiState>(AllDiariesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        // TODO: it's correct?
        onStart()
    }

    // load diaries
    private fun onStart() {
        auth.uid?.let { userId ->
            viewModelScope.launch {
                val diaries = repository.getUserDiaries(userId)
                val allDiaries = AllDiaries(userId, diaries.map { diary -> diary.id })
                val diariesMap = diaries.associateBy { diary -> diary.id }
                val diaryCoversSet = diaries.map { diary -> diary.coverId }.toSet()
                val diaryCoversMap = diaryCoversSet.associateWith { diaryCoverId -> repository.getDiaryCover(diaryCoverId) }
                val diaryPhotosMap = diaries.map { diary -> diary.id }.associateWith { diaryId -> repository.getAllDiaryImages(diaryId) }
                _uiState.value = AllDiariesUiState.Success(allDiaries, diariesMap, diaryCoversMap, diaryPhotosMap)
            }
        }
    }

    fun overwriteDiary(updatedDiary: Diary?) {
        val state = uiState.value
        if (state !is AllDiariesUiState.Success) return

        if (updatedDiary != null) {
            viewModelScope.launch {
                val diariesMap = state.diariesMap.toMutableMap()
                diariesMap[updatedDiary.id] = updatedDiary
                val diaryCoversMap = state.diaryCoversMap.toMutableMap()

                val diaryCoverId = updatedDiary.coverId
                if (diaryCoverId !in diaryCoversMap) {
                    diaryCoversMap[diaryCoverId] = repository.getDiaryCover(diaryCoverId)
                }

                _uiState.update {
                    AllDiariesUiState.Success(state.allDiaries, diariesMap, diaryCoversMap, state.diaryPhotosMap, state.pagerState, state.buttonState)
                }
            }
        }
    }

    fun onButtonStateUpdate(newButtonState: ButtonState) {
        val state = uiState.value
        if (state !is AllDiariesUiState.Success) return

        _uiState.update {
            AllDiariesUiState.Success(
                state.allDiaries,
                state.diariesMap,
                state.diaryCoversMap,
                state.diaryPhotosMap,
                state.pagerState,
                newButtonState
            )
        }
    }

    fun onDeleteDiary(diaryId: String, onSuccess: () -> Unit = {}){
        viewModelScope.launch {
            runCatching {
                repository.deleteDiary(diaryId)
            }.onSuccess {
                onSuccess()
            }.onFailure { e ->
                Log.d("Delete diary", e.toString())
            }
        }
    }
}
