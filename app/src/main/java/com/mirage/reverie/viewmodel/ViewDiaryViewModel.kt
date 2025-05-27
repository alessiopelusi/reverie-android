package com.mirage.reverie.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.data.model.DiarySubPage
import com.mirage.reverie.data.repository.DiaryRepository
import com.mirage.reverie.navigation.ViewDiaryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.min

sealed class ViewDiaryUiState {
    data object Loading : ViewDiaryUiState()
    data class Success(
        val diary: Diary,
        val pagesMap: Map<String, DiaryPage>,
        val subPagesMap: Map<String, DiarySubPage>,
        val imagesMap: Map<String, DiaryImage>
    ) : ViewDiaryUiState() {
        val pages: List<DiaryPage>
            get() = diary.pageIds.map { pageId -> pagesMap.getValue(pageId) }

        val subPages: List<DiarySubPage>
            get() = pages.flatMap { page -> page.subPageIds }.map { subPageId -> subPagesMap.getValue(subPageId) }

        val subPageImagesMap: Map<String, List<DiaryImage>>
            get() = subPages.map { subPage -> subPage.id }.associateWith { subPageId -> subPagesMap.getValue(subPageId).imageIds.map { imageId -> imagesMap.getValue(imageId) } }

        val images: List<DiaryImage>
            get() = subPages.flatMap { subPage -> subPage.imageIds }.map { imageId -> imagesMap.getValue(imageId) }
    }
    data class Error(val exception: Throwable) : ViewDiaryUiState()
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class ViewDiaryViewModel @Inject constructor(
    private val context: Context,
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
    private val _uiState = MutableStateFlow<ViewDiaryUiState>(ViewDiaryUiState.Loading)
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
            val imagesMap = subPagesMap.values.flatMap { subPage -> subPage.imageIds }.associateWith { imageId -> repository.getDiaryImage(imageId) }
            var newState: ViewDiaryUiState = ViewDiaryUiState.Success(diary, pagesMap, subPagesMap, imagesMap)

            // loading images bitmap
            imagesMap.keys.forEach { diaryImageId ->
                newState = loadImage(diaryImageId, newState)
            }

            val state = newState as ViewDiaryUiState.Success
            // add empty page if needed
            newState = addEmptyPage(state)

            _uiState.update { newState }
        }
    }

    private suspend fun addEmptyPage(state: ViewDiaryUiState): ViewDiaryUiState {
        if (state !is ViewDiaryUiState.Success) return state
        // if no necessity to add empty page return
        if (state.pages.isNotEmpty() && state.pages.last().date == LocalDate.now()) return state

        // if last page has no content and no images we can simply change the date
        if (state.pages.isNotEmpty() && state.pages.last().content.isEmpty() && state.subPages.last().imageIds.isEmpty()) {
            val page = state.pages.last().copy(timestamp = Timestamp.now())
            repository.updatePage(page)

            val updatedPagesMap = state.pagesMap.toMutableMap()
            updatedPagesMap[page.id] = page

            return ViewDiaryUiState.Success(state.diary, updatedPagesMap, state.subPagesMap, state.imagesMap)
        } else {
            val pageWithId = repository.savePage(DiaryPage(diaryId = state.diary.id))

            val updatedPagesMap = state.pagesMap.toMutableMap()
            updatedPagesMap[pageWithId.id] = pageWithId

            val updatedPagesIds = state.diary.pageIds.toMutableList()
            updatedPagesIds.add(pageWithId.id)
            val updatedDiary = state.diary.copy(pageIds = updatedPagesIds)

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            val firstSubPage = pageWithId.subPageIds.first()
            updatedSubPagesMap[firstSubPage] = repository.getSubPage(firstSubPage)

            return ViewDiaryUiState.Success(updatedDiary, updatedPagesMap, updatedSubPagesMap, state.imagesMap)
        }
    }

    fun overwritePage(page: DiaryPage?) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        if (page != null) {
            val pagesMap = state.pagesMap.toMutableMap()
            pagesMap[page.id] = page

            val subPagesMap = state.subPagesMap.toMutableMap()
            page.subPageIds.forEach{ subPageId ->
                subPagesMap[subPageId] = subPagesMap.getValue(subPageId).copy(testOverflow = 0, contentEndIndex = 0)
            }
            Log.d("overwritePage", state.subPagesMap.toString())
            Log.d("overwritePage", subPagesMap.toString())

            _uiState.update {
                ViewDiaryUiState.Success(state.diary, pagesMap, subPagesMap, state.imagesMap)
            }
        }
    }

    fun loadImage(imageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val imagesMap = state.imagesMap.toMutableMap()
        var image = imagesMap.getValue(imageId)

        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(image.url)
            //.allowHardware(false) // Required if you want to manipulate the bitmap later
            .build()

        viewModelScope.launch {
            val result = imageLoader.execute(request)
            val bitmap = (result as SuccessResult).image.toBitmap()
            image = image.copy(bitmap = bitmap)
            imagesMap[imageId] = image

            _uiState.update {
                ViewDiaryUiState.Success(state.diary, state.pagesMap, state.subPagesMap, imagesMap)
            }
        }
    }


    suspend fun loadImage(imageId: String, state: ViewDiaryUiState): ViewDiaryUiState {
        if (state !is ViewDiaryUiState.Success) return state

        val imagesMap = state.imagesMap.toMutableMap()
        var image = imagesMap.getValue(imageId)

        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(image.url)
            //.allowHardware(false) // Required if you want to manipulate the bitmap later
            .build()

        val result = imageLoader.execute(request)
        val bitmap = (result as SuccessResult).image.toBitmap()
        image = image.copy(bitmap = bitmap)
        imagesMap[imageId] = image

        return ViewDiaryUiState.Success(state.diary, state.pagesMap, state.subPagesMap, imagesMap)
    }

    private fun getPageSubPages(pageId: String): List<DiarySubPage> {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return listOf()

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap

        val page = pagesMap.getValue(pageId)
        return page.subPageIds.map { subPageId -> subPagesMap.getValue(subPageId) }
    }


    private fun getPreviousSubPage(subPageId: String): DiarySubPage? {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return null

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val pageSubPages = getPageSubPages(subPage.pageId)
        val subPageIndex = pageSubPages.indexOf(subPage)
        if (subPageIndex == 0) return null

        return pageSubPages[subPageIndex-1]
    }

    private fun getNextSubPage(subPageId: String): DiarySubPage? {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return null

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
        if (state !is ViewDiaryUiState.Success) return 0

        val previousSubPage = getPreviousSubPage(subPageId)
        return previousSubPage?.contentEndIndex ?: 0
    }

    fun getSubPageContent(subPageId: String): String {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return ""

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap
        val pageId = subPagesMap.getValue(subPageId).pageId
        val page = pagesMap.getValue(pageId)

        return page.content.substring(startIndex = getSubPageStartIndex(subPageId))
    }

    private fun updateSubPage(updatedSubPage: DiarySubPage) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        // if equals to current, return
        if (updatedSubPage == state.subPagesMap.getValue(updatedSubPage.id)) return

        // firebase update
        //if (updatedSubPage != state.subPagesMap.getValue(updatedSubPage.id)) {
        viewModelScope.launch {
            repository.updateSubPage(updatedSubPage)
        }
        //}

        // local update
        val updatedSubPagesMap = state.subPagesMap.toMutableMap()
        updatedSubPagesMap[updatedSubPage.id] = updatedSubPage

        _uiState.update {
            ViewDiaryUiState.Success(
                state.diary,
                state.pagesMap,
                updatedSubPagesMap,
                state.imagesMap
            )
        }
    }

    private fun updateSubPageContentEndIndex(subPageId: String, contentEndIndex: Int) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val updatedSubPage = subPage.copy(contentEndIndex = contentEndIndex, cipolla = subPage.cipolla+1)

        updateSubPage(updatedSubPage)
    }

    private fun resetSubPageContentEndIndex(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val pagesMap = state.pagesMap
        val pageId = subPagesMap.getValue(subPageId).pageId
        val page = pagesMap.getValue(pageId)

        updateSubPageContentEndIndex(subPageId, page.content.length)
    }

    private fun updateSubPageTestOverflow(subPageId: String, testOverflow: Int) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val updatedSubPage = subPage.copy(testOverflow = testOverflow, cipolla = subPage.cipolla+1)

        updateSubPage(updatedSubPage)
    }

    private fun incrementSubPageTestOverflow(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)

        updateSubPageTestOverflow(subPageId, subPage.testOverflow+1)
    }

    fun resetSubPageTestOverflow(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        updateSubPageContentEndIndex(subPageId, 0)
    }

    private fun addSubPage(subPage: DiarySubPage) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        viewModelScope.launch {
            val subPageWithId = repository.saveSubPage(subPage)

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            updatedSubPagesMap[subPageWithId.id] = subPageWithId

            val updatedPagesMap = state.pagesMap.toMutableMap()
            val updatedPage = updatedPagesMap.getValue(subPageWithId.pageId)
            val updatedSubPagesIds = updatedPage.subPageIds.toMutableList()
            updatedSubPagesIds.add(subPageWithId.id)

            updatedPagesMap[subPageWithId.pageId] = updatedPage.copy(subPageIds = updatedSubPagesIds)

            _uiState.update {
                ViewDiaryUiState.Success(
                    state.diary,
                    updatedPagesMap,
                    updatedSubPagesMap,
                    state.imagesMap
                )
            }
        }
    }

    private fun deleteSubPage(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        viewModelScope.launch {
            repository.deleteSubPage(subPageId)

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            val subPage = updatedSubPagesMap.getValue(subPageId)
            updatedSubPagesMap.remove(subPageId)

            val updatedPagesMap = state.pagesMap.toMutableMap()
            val updatedPage = updatedPagesMap.getValue(subPage.pageId)
            val updatedSubPagesIds = updatedPage.subPageIds.toMutableList()
            updatedSubPagesIds.remove(subPageId)

            updatedPagesMap[subPage.pageId] = updatedPage.copy(subPageIds = updatedSubPagesIds)

            _uiState.update{
                ViewDiaryUiState.Success(
                    state.diary,
                    updatedPagesMap,
                    updatedSubPagesMap,
                    state.imagesMap
                )
            }
        }
    }

    fun updateSubPageOffset(subPageId: String, offset: Int) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        var subPage = state.subPagesMap.getValue(subPageId)
        val pageId = state.subPagesMap.getValue(subPageId).pageId

        when(subPage.testOverflow) {
            0 -> {
                resetSubPageContentEndIndex(subPageId)
            }
            2 -> {
                // if empty page, page not last and page without images
                if (offset == 1 && getPreviousSubPage(subPageId) != null && subPage.imageIds.isEmpty()) {
                    deleteSubPage(subPage.id)
                    return
                }
                val page = state.pagesMap.getValue(pageId)
                updateSubPageContentEndIndex(subPageId,
                    min(getSubPageStartIndex(subPageId) + offset, page.content.length)
                )

                // update subPage using fresh state value
                subPage = (uiState.value as ViewDiaryUiState.Success).subPagesMap.getValue(subPageId)

                // if not last page
                val nextSubPage = getNextSubPage(subPageId)
                if (nextSubPage != null) {
                    updateSubPageContentEndIndex(nextSubPage.id, page.content.length)
                    resetSubPageTestOverflow(nextSubPage.id)
                } else if (subPage.contentEndIndex < page.content.length) {
                    addSubPage(
                        DiarySubPage(
                            pageId = pageId,
                            diaryId = page.diaryId,
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

    fun updateDiaryImage(updatedDiaryImage: DiaryImage, locally: Boolean = false) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        if (!locally) viewModelScope.launch {
            repository.updateDiaryImage(updatedDiaryImage)
        }

        val updatedImageMap = state.imagesMap.toMutableMap()
        updatedImageMap[updatedDiaryImage.id] = updatedDiaryImage

        _uiState.update {
            ViewDiaryUiState.Success(
                state.diary,
                state.pagesMap,
                state.subPagesMap,
                updatedImageMap
            )
        }
    }

    fun updateDiaryImageTransform(diaryImageId: String, offsetX: Int, offsetY: Int, scale: Float, rotation: Float, locally: Boolean = false) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val imageMap = state.imagesMap
        val diaryImage = imageMap.getValue(diaryImageId)

        // should never be null (if we update, we loaded the image)
        val updatedDiaryImage = diaryImage.copy(offsetX = offsetX, offsetY = offsetY, scale = scale, rotation = rotation)

        updateDiaryImage(updatedDiaryImage, locally = locally)
    }

    fun deleteImage(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        viewModelScope.launch {
            repository.deleteDiaryImage(diaryImageId)

            val updatedImagesMap = state.imagesMap.toMutableMap()
            val diaryImage = updatedImagesMap.getValue(diaryImageId)
            updatedImagesMap.remove(diaryImageId)

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            val updatedSubPage = updatedSubPagesMap.getValue(diaryImage.subPageId)
            val updatedImageIds = updatedSubPage.imageIds.toMutableList()
            updatedImageIds.remove(diaryImageId)

            updatedSubPagesMap[diaryImage.subPageId] = updatedSubPage.copy(imageIds = updatedImageIds)

            _uiState.update {
                ViewDiaryUiState.Success(
                    state.diary,
                    state.pagesMap,
                    updatedSubPagesMap,
                    updatedImagesMap
                )
            }
        }

    }

    fun moveImageUp(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val imageMap = state.imagesMap
        val diaryImage = imageMap.getValue(diaryImageId)
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val updatedImageIds = subPage.imageIds.toMutableList()
        val imageIndex = updatedImageIds.indexOf(diaryImageId)
        if (imageIndex < updatedImageIds.size-1) { // Ensure the index is valid and not the last element
            updatedImageIds.removeAt(imageIndex)  // Remove the element at the current index
            updatedImageIds.add(imageIndex+1, diaryImageId)       // Insert it at the previous index
            updateSubPage(subPage.copy(imageIds = updatedImageIds))
        }
    }

    fun moveImageDown(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        if (!state.imagesMap.containsKey(diaryImageId)) return

        val imageMap = state.imagesMap
        val diaryImage = imageMap.getValue(diaryImageId)
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val updatedImageIds = subPage.imageIds.toMutableList()
        val imageIndex = updatedImageIds.indexOf(diaryImageId)
        if (imageIndex > 0) { // Ensure the index is valid and not the first element
            updatedImageIds.removeAt(imageIndex)  // Remove the element at the current index
            updatedImageIds.add(imageIndex-1, diaryImageId)       // Insert it at the previous index
            updateSubPage(subPage.copy(imageIds = updatedImageIds))
        }
    }

    fun isLastSubPageImage(diaryImageId: String): Boolean {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return false

        if (!state.imagesMap.containsKey(diaryImageId)) return false

        val imageMap = state.imagesMap
        val diaryImage = imageMap.getValue(diaryImageId)
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val imageIndex = subPage.imageIds.indexOf(diaryImageId)
        return imageIndex == subPage.imageIds.size-1
    }

    fun isFirstSubPageImage(diaryImageId: String): Boolean {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return false

        if (!state.imagesMap.containsKey(diaryImageId)) return false

        val imageMap = state.imagesMap
        val diaryImage = imageMap.getValue(diaryImageId)
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val imageIndex = subPage.imageIds.indexOf(diaryImageId)
        return imageIndex == 0
    }
}
