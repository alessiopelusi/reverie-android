package com.mirage.reverie.viewmodel

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
import kotlin.math.max

fun createPagerState(size: Int, selectedDiary: Int = size/2) : PagerState =
    PagerState(
        // endlessPagerMultiplier = 1000
        // endlessPagerMultiplier/2 = 500
        // offset = 1
        pageCount = {if (size > 1) size*1000 else 1},
        currentPage = if (size > 1) size*500 + selectedDiary else 0
    )

enum class ButtonState {
    INFO,
    IMAGES,
    //VIDEOS
}

sealed class AllDiariesUiState {
    data object Loading : AllDiariesUiState()
    data class Success(
        val allDiaries: AllDiaries,
        val diariesMap: Map<String, Diary>,
        val diaryCoversMap: Map<String, DiaryCover>,
        val diaryPhotosMap: Map<String, List<DiaryImage>>,
        val pagerState: PagerState = createPagerState(allDiaries.diaryIds.size),
        val buttonState: ButtonState = ButtonState.INFO,
        val deleteDialogState: Boolean = false
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
                val diaryIds = state.allDiaries.diaryIds.toMutableList()
                val diaryPhotosMap = state.diaryPhotosMap.toMutableMap()
                var pagerState = state.pagerState
                if (!diaryIds.contains(updatedDiary.id)) {
                    diaryIds.add(updatedDiary.id)
                    diaryPhotosMap[updatedDiary.id] = listOf()
                    pagerState = createPagerState(diaryIds.size, diaryIds.size-1)
                }

                val allDiaries = AllDiaries(state.allDiaries.userId, diaryIds)

                val diariesMap = state.diariesMap.toMutableMap()
                diariesMap[updatedDiary.id] = updatedDiary

                val diaryCoversMap = state.diaryCoversMap.toMutableMap()
                val diaryCoverId = updatedDiary.coverId
                if (diaryCoverId !in diaryCoversMap) {
                    diaryCoversMap[diaryCoverId] = repository.getDiaryCover(diaryCoverId)
                }

                _uiState.update {
                    AllDiariesUiState.Success(
                        allDiaries,
                        diariesMap,
                        diaryCoversMap,
                        diaryPhotosMap,
                        pagerState,
                        state.buttonState
                    )
                }
            }
        }
    }

    fun overwriteImages(updatedImages: List<DiaryImage>? = listOf()) {
        val state = uiState.value
        if (state !is AllDiariesUiState.Success) return

        if (!updatedImages.isNullOrEmpty()) {
            viewModelScope.launch {
                val diaryPhotosMap = state.diaryPhotosMap.toMutableMap()
                diaryPhotosMap[updatedImages.first().diaryId] = updatedImages

                _uiState.update {
                    AllDiariesUiState.Success(
                        state.allDiaries,
                        state.diariesMap,
                        state.diaryCoversMap,
                        diaryPhotosMap,
                        state.pagerState,
                        state.buttonState
                    )
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
                newButtonState,
            )
        }
    }

    private fun onUpdateDeleteDiaryDialog(newDeleteDialogState: Boolean) {
        val state = uiState.value
        if (state !is AllDiariesUiState.Success) return

        _uiState.update {
            AllDiariesUiState.Success(
                state.allDiaries,
                state.diariesMap,
                state.diaryCoversMap,
                state.diaryPhotosMap,
                state.pagerState,
                state.buttonState,
                newDeleteDialogState
            )
        }
    }

    fun onCloseDeleteDiaryDialog() {
        onUpdateDeleteDiaryDialog(false)
    }

    fun onOpenDeleteDiaryDialog() {
        onUpdateDeleteDiaryDialog(true)
    }

    fun onDeleteDiary(diaryId: String){
        val state = uiState.value
        if (state !is AllDiariesUiState.Success) return

        viewModelScope.launch {
            repository.deleteDiary(diaryId)

            val diaryIds = state.allDiaries.diaryIds.toMutableList()
            val diaryPhotosMap = state.diaryPhotosMap.toMutableMap()
            diaryIds.remove(diaryId)
            diaryPhotosMap.remove(diaryId)

            val allDiaries = AllDiaries(state.allDiaries.userId, diaryIds)

            val diariesMap = state.diariesMap.toMutableMap()
            diariesMap.remove(diaryId)

            val pagerState = createPagerState(diaryIds.size, max((state.pagerState.currentPage % (diaryIds.size + 1)) - 1, 0))

            _uiState.update {
                AllDiariesUiState.Success(
                    allDiaries,
                    diariesMap,
                    state.diaryCoversMap,
                    diaryPhotosMap,
                    pagerState,
                    state.buttonState,
                    false
                )
            }
        }
    }
}
