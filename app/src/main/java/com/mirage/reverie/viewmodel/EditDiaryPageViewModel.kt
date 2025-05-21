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
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class EditDiaryPageUiState {
    data object Loading : EditDiaryPageUiState()
    data class Success(
        val page: DiaryPage,
        val subPagesMap: Map<String, DiarySubPage>,
        val imagesMap: Map<String, DiaryImage?> // Lazy loading of images
    ) : EditDiaryPageUiState() {
        val subPages: List<DiarySubPage>
            get() = page.subPageIds.map { subPageId -> subPagesMap.getValue(subPageId) }
    }
    data class Error(val exception: Throwable) : EditDiaryPageUiState()
}

@HiltViewModel
class EditDiaryPageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository
) : ViewModel() {
    private val page = savedStateHandle.toRoute<EditDiaryPageRoute>()

    private val _uiState = MutableStateFlow<EditDiaryPageUiState>(EditDiaryPageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadPage(page.pageId)
    }

    private fun loadPage(pageId: String) {
        viewModelScope.launch {
            val page = repository.getPage(pageId)
            val subPagesMap = page.subPageIds.associateWith { subPageId -> repository.getSubPage(subPageId) }
            val imagesMap = subPagesMap.values.flatMap { subPage -> subPage.imageIds }.associateWith { null }
            _uiState.value = EditDiaryPageUiState.Success(page, subPagesMap, imagesMap)
        }
    }
}
