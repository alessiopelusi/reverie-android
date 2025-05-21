package com.mirage.reverie.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.data.toFirestoreMap
import com.mirage.reverie.navigation.ViewDiaryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

sealed class DiaryUiState {
    data object Loading : DiaryUiState()
    data class Success(
        val diary: Diary,
        val pagesMap: Map<String, DiaryPage>,
        val subPagesMap: Map<String, DiarySubPage>,
        val imagesMap: Map<String, DiaryImage?> // Lazy loading of images
    ) : DiaryUiState() {
        val pages: List<DiaryPage>
            get() = diary.pageIds.map { pageId -> pagesMap.getValue(pageId) }

        val subPages: List<DiarySubPage>
            get() = pages.flatMap { page -> page.subPageIds }.map { subPageId -> subPagesMap.getValue(subPageId) }
    }
    data class Error(val exception: Throwable) : DiaryUiState()
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository,
) : ViewModel() {
    private val diaryId = savedStateHandle.toRoute<ViewDiaryRoute>().diaryId
    // Expose screen UI state
    /*
        val uiState : StateFlow<DiaryState> = repository.getDiaryById(diary.diaryId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = repository.getDiaryById(diary.diaryId).value // Wrong?
        )
    */
    private val _uiState = MutableStateFlow<DiaryUiState>(DiaryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        onStart()
    }

    // load diary
    private fun onStart() {
        viewModelScope.launch {
            val diary = repository.getDiary(diaryId)
            val pagesMap = diary.pageIds.associateWith { pageId -> repository.getPage(pageId) }
            val subPagesMap = pagesMap.values.flatMap { page -> page.subPageIds }.associateWith { subPageId -> repository.getSubPage(subPageId) }
            val imagesMap = subPagesMap.values.flatMap { subPage -> subPage.imageIds }.associateWith { null }
            _uiState.value = DiaryUiState.Success(diary, pagesMap, subPagesMap, imagesMap)
        }
    }

    private fun getPageSubPages(pageId: String): List<DiarySubPage> {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return listOf()

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap

        val page = pagesMap.getValue(pageId)
        return page.subPageIds.map { subPageId -> subPagesMap.getValue(subPageId) }
    }


    private fun getPreviousSubPage(subPageId: String): DiarySubPage? {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return null

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val pageSubPages = getPageSubPages(subPage.pageId)
        val subPageIndex = pageSubPages.indexOf(subPage)
        if (subPageIndex == 0) return null

        return pageSubPages[subPageIndex-1]
    }

    private fun getNextSubPage(subPageId: String): DiarySubPage? {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return null

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val page = pagesMap.getValue(subPage.pageId)
        val pageSubPages = getPageSubPages(subPage.pageId)
        val subPageIndex = pageSubPages.indexOf(subPage)
        if (subPageIndex+1 == page.subPageIds.size) return null

        return pageSubPages[subPageIndex+1]
    }

    private fun getSubPageStartIndex(subPageId: String): Int {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return 0

        val previousSubPage = getPreviousSubPage(subPageId)
        return previousSubPage?.contentEndIndex ?: 0
    }

    fun getSubPageContent(subPageId: String): String {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return ""

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap
        val pageId = subPagesMap.getValue(subPageId).pageId
        val page = pagesMap.getValue(pageId)

        return page.content.substring(startIndex = getSubPageStartIndex(subPageId))
    }

    fun getSubPageImages(subPageId: String): List<DiaryImage> {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return listOf()

        val subPagesMap = state.subPagesMap
        val updatedImageMap = state.imagesMap.toMutableMap()
        val subPage = subPagesMap.getValue(subPageId)

        viewModelScope.launch {
            subPage.imageIds.forEach { imageId ->
                if (updatedImageMap.getValue(imageId) == null) {
                    updatedImageMap[imageId] = repository.getDiaryImage(imageId)
                }
                updatedImageMap.getValue(imageId)
            }

            _uiState.value = DiaryUiState.Success(
                state.diary,
                state.pagesMap,
                state.subPagesMap,
                updatedImageMap
            )
        }

        return subPage.imageIds.mapNotNull { imageId -> updatedImageMap[imageId] }
    }

    private fun updateSubPage(updatedSubPage: DiarySubPage) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        // if equals to current, return
        if (updatedSubPage == state.subPagesMap.getValue(updatedSubPage.id)) return

        // firebase update
        if (updatedSubPage.toFirestoreMap() != state.subPagesMap.getValue(updatedSubPage.id).toFirestoreMap()) {
            viewModelScope.launch {
                repository.updateSubPage(updatedSubPage)
            }
        }

        // local update
        val updatedSubPagesMap = state.subPagesMap.toMutableMap()
        updatedSubPagesMap[updatedSubPage.id] = updatedSubPage

        _uiState.value = DiaryUiState.Success(
            state.diary,
            state.pagesMap,
            updatedSubPagesMap,
            state.imagesMap
        )
    }

    private fun updateSubPageContentEndIndex(subPageId: String, contentEndIndex: Int) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val updatedSubPage = subPage.copy(contentEndIndex = contentEndIndex, cipolla = subPage.cipolla+1)

        updateSubPage(updatedSubPage)
    }

    private fun resetSubPageContentEndIndex(subPageId: String) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap
        val pageId = subPagesMap.getValue(subPageId).pageId
        val page = pagesMap.getValue(pageId)

        updateSubPageContentEndIndex(subPageId, page.content.length)
    }

    private fun updateSubPageTestOverflow(subPageId: String, testOverflow: Int) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val updatedSubPage = subPage.copy(testOverflow = testOverflow, cipolla = subPage.cipolla+1)

        updateSubPage(updatedSubPage)
    }

    private fun incrementSubPageTestOverflow(subPageId: String) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)

        updateSubPageTestOverflow(subPageId, subPage.testOverflow+1)
    }

    fun resetSubPageTestOverflow(subPageId: String) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        updateSubPageContentEndIndex(subPageId, 0)
    }

    private fun addSubPage(subPage: DiarySubPage) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        viewModelScope.launch {
            val subPageWithId = repository.saveSubPage(subPage)

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            updatedSubPagesMap[subPageWithId.id] = subPageWithId

            val updatedPagesMap = state.pagesMap.toMutableMap()
            val updatedPage = updatedPagesMap.getValue(subPageWithId.pageId)
            val updatedSubPagesIds = updatedPage.subPageIds.toMutableList()
            updatedSubPagesIds.add(subPageWithId.id)

            updatedPagesMap[subPageWithId.pageId] = updatedPage.copy(subPageIds = updatedSubPagesIds)

            _uiState.value = DiaryUiState.Success(state.diary, updatedPagesMap, updatedSubPagesMap, state.imagesMap)
        }
    }

    fun updateSubPageOffset(subPageId: String, offset: Int) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        var subPage = state.subPagesMap.getValue(subPageId)
        val pageId = state.subPagesMap.getValue(subPageId).pageId

        when(subPage.testOverflow) {
            0 -> {
                resetSubPageContentEndIndex(subPageId)
            }
            2 -> {
                val page = state.pagesMap.getValue(pageId)
                updateSubPageContentEndIndex(subPageId,
                    min(getSubPageStartIndex(subPageId) + offset, page.content.length)
                )

                // update subPage using fresh state value
                subPage = (uiState.value as DiaryUiState.Success).subPagesMap.getValue(subPageId)

                // if not last page
                val nextSubPage = getNextSubPage(subPageId)
                if (nextSubPage != null) {
                    updateSubPageContentEndIndex(nextSubPage.id, page.content.length)
                    resetSubPageTestOverflow(nextSubPage.id)
                } else if (subPage.contentEndIndex < page.content.length) {
                    addSubPage(
                        DiarySubPage(
                            pageId = pageId,
                            contentEndIndex = page.content.length
                        )
                    )
                }
            }
        }

        // we update one page at a time
        if (subPage.testOverflow < 3) {
            val prevSubPage = getPreviousSubPage(subPageId)
            if (prevSubPage == null || prevSubPage.testOverflow == 3)
                incrementSubPageTestOverflow(subPageId)
        }
    }

    private fun updateDiaryImage(updatedDiaryImage: DiaryImage) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        viewModelScope.launch {
            repository.updateDiaryImage(updatedDiaryImage)
            val updatedImageMap = state.imagesMap.toMutableMap()
            updatedImageMap[updatedDiaryImage.id] = updatedDiaryImage

            _uiState.value = DiaryUiState.Success(
                state.diary,
                state.pagesMap,
                state.subPagesMap,
                updatedImageMap
            )
        }
    }

    fun updateDiaryImageOffset(diaryImageId: String, offset: Offset) {
        val state = uiState.value
        if (state !is DiaryUiState.Success) return

        viewModelScope.launch {
            val imageMap = state.imagesMap
            val diaryImage = imageMap.getValue(diaryImageId)

            // should never be null (if we update, we loaded the image)
            if (diaryImage != null) {
                val updatedDiaryImage = diaryImage.copy(offset = offset)

                updateDiaryImage(updatedDiaryImage)
            }
        }
    }
}
