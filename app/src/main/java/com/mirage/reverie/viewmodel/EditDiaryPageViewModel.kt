package com.mirage.reverie.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.navigation.EditDiaryPageRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



data class EditDiaryPageFormState(
    val page: DiaryPage = DiaryPage(),
    val subPagesMap: Map<String, DiarySubPage> = mapOf(),
    val imagesMap: Map<String, DiaryImage> = mapOf()// Lazy loading of images
)


sealed class EditDiaryPageUiState {
    data object Loading : EditDiaryPageUiState()
    data object Idle: EditDiaryPageUiState()
    data object Success : EditDiaryPageUiState()
}

@HiltViewModel
class EditDiaryPageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository
) : ViewModel() {
    private val pageId = savedStateHandle.toRoute<EditDiaryPageRoute>().pageId

    private val _uiState = MutableStateFlow<EditDiaryPageUiState>(EditDiaryPageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(EditDiaryPageFormState())
    val formState = _formState.asStateFlow()

    init {
        onStart()
    }

    private fun onStart() {
        viewModelScope.launch {
            val page = repository.getPage(pageId)
            val subPagesMap = page.subPageIds.associateWith { subPageId -> repository.getSubPage(subPageId) }
            val imagesMap = subPagesMap.values.flatMap { subPage -> subPage.imageIds }.associateWith { imageId -> repository.getDiaryImage(imageId) }

            _uiState.update { EditDiaryPageUiState.Idle }
            _formState.update { EditDiaryPageFormState(page, subPagesMap, imagesMap) }
        }
    }


    fun onUpdatePage() {
        _uiState.update { EditDiaryPageUiState.Idle }

        val state = formState.value

        viewModelScope.launch {
            try {
                repository.updatePage(state.page)
                _uiState.update { EditDiaryPageUiState.Success }
            } catch (_: Exception) {
            }
        }
    }

    // Handle business logic
    fun onUpdateContent(newContent: String) {
        val currentState = uiState.value
        if (currentState is EditDiaryPageUiState.Loading) return

        _formState.update { state ->
            val updatedPage = state.page.copy(content = newContent)
            state.copy(page = updatedPage)
        }
    }
}
