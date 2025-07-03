package com.mirage.reverie.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.core.graphics.scale
import com.mirage.reverie.toLocalDate

sealed class ViewDiaryUiState {
    data object Loading : ViewDiaryUiState()
    data class Success(
        val diary: Diary,
        val pagesMap: Map<String, DiaryPage>,
        val subPagesMap: Map<String, DiarySubPage>,
        val imagesMap: Map<String, DiaryImage>,
        val diaryPageListState: LazyListState,
        val deleteDialogState: Boolean = false
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

@HiltViewModel
class ViewDiaryViewModel @Inject constructor(
    private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository,
) : ViewModel() {
    private val diaryId = savedStateHandle.toRoute<ViewDiaryRoute>().diaryId

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
            val imagesMap = subPagesMap.values.flatMap { subPage -> subPage.imageIds }.associateWith { imageId -> loadImageBitmap(repository.getDiaryImage(imageId)) }
            var newState: ViewDiaryUiState = ViewDiaryUiState.Success(diary, pagesMap, subPagesMap, imagesMap, LazyListState())

            val state = newState as ViewDiaryUiState.Success
            // add empty page if needed
            newState = addEmptyPage(state) as ViewDiaryUiState.Success

            newState = newState.copy(diaryPageListState = LazyListState(firstVisibleItemIndex = newState.subPages.lastIndex))

            _uiState.update { newState }
        }
    }

    private suspend fun addEmptyPage(state: ViewDiaryUiState): ViewDiaryUiState {
        if (state !is ViewDiaryUiState.Success) return state
        // if no necessity to add empty page return
        if (state.pages.isNotEmpty() && state.pages.last().date.toLocalDate() == LocalDate.now()) return state

        // if last page has no content and no images we can simply change the date
        if (state.pages.isNotEmpty() && state.pages.last().content.isEmpty() && state.subPages.last().imageIds.isEmpty()) {
            val page = state.pages.last().copy(timestamp = Timestamp.now())
            repository.updatePage(page)

            val updatedPagesMap = state.pagesMap.toMutableMap()
            updatedPagesMap[page.id] = page

            return ViewDiaryUiState.Success(state.diary, updatedPagesMap, state.subPagesMap, state.imagesMap, state.diaryPageListState)
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

            return ViewDiaryUiState.Success(updatedDiary, updatedPagesMap, updatedSubPagesMap, state.imagesMap, state.diaryPageListState)
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

            _uiState.update {
                ViewDiaryUiState.Success(state.diary, pagesMap, subPagesMap, state.imagesMap, state.diaryPageListState)
            }
        }
    }

    fun uploadImage(imageUri: Uri, subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        if (imageUri.path == null) return

        viewModelScope.launch {
            val subPage = state.subPagesMap.getValue(subPageId)

            val diaryImageWithId = repository.saveDiaryImage(
                DiaryImage(
                    subPageId = subPageId,
                    diaryId = subPage.diaryId,
                ),
                imageUri
            )

            val diaryImage = loadImageBitmap(diaryImageWithId)

            val updatedImagesMap = state.imagesMap.toMutableMap()
            updatedImagesMap[diaryImage.id] = diaryImage

            val updatedSubPagesMap = state.subPagesMap.toMutableMap()
            val updatedSubPage = updatedSubPagesMap.getValue(subPageId)
            val updatedImagesIds = updatedSubPage.imageIds.toMutableList()
            updatedImagesIds.add(diaryImage.id)

            updatedSubPagesMap[subPageId] = updatedSubPage.copy(imageIds = updatedImagesIds)

            _uiState.update {
                ViewDiaryUiState.Success(
                    state.diary,
                    state.pagesMap,
                    updatedSubPagesMap,
                    updatedImagesMap,
                    state.diaryPageListState
                )
            }
        }
    }

    private suspend fun loadImageBitmap(image: DiaryImage): DiaryImage {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(image.url)
            .build()

        val result = imageLoader.execute(request)
        var bitmap = (result as SuccessResult).image.toBitmap()

        // TODO: hack used to reduce bitmap size. Otherwise it would be too big (we use the size of the bitmap in calculations)
        while (bitmap.width > context.resources.displayMetrics.widthPixels/2 || bitmap.height > context.resources.displayMetrics.heightPixels/2)
            bitmap = bitmap.scale(bitmap.width / 2, bitmap.height / 2)

        return image.copy(bitmap = bitmap)
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
        viewModelScope.launch {
            repository.updateSubPage(updatedSubPage)
        }

        // local update
        val updatedSubPagesMap = state.subPagesMap.toMutableMap()
        updatedSubPagesMap[updatedSubPage.id] = updatedSubPage

        _uiState.update {
            ViewDiaryUiState.Success(
                state.diary,
                state.pagesMap,
                updatedSubPagesMap,
                state.imagesMap,
                state.diaryPageListState
            )
        }
    }

    private fun updateSubPageContentEndIndex(subPageId: String, contentEndIndex: Int) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)
        val updatedSubPage = subPage.copy(contentEndIndex = contentEndIndex, refreshCounter = subPage.refreshCounter+1)

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
        val updatedSubPage = subPage.copy(testOverflow = testOverflow, refreshCounter = subPage.refreshCounter+1)

        updateSubPage(updatedSubPage)
    }

    private fun incrementSubPageTestOverflow(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(subPageId)

        updateSubPageTestOverflow(subPageId, subPage.testOverflow+1)
    }

    private fun resetSubPageTestOverflow(subPageId: String) {
        updateSubPageTestOverflow(subPageId, 0)
    }

    private fun resetSubPageTextCalculation(subPageId: String) {
        resetSubPageTestOverflow(subPageId)
        resetSubPageContentEndIndex(subPageId)
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
                    state.imagesMap,
                    state.diaryPageListState
                )
            }
        }
    }

    private fun deleteSubPage(subPageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val subPage = state.subPagesMap[subPageId] ?: return

        viewModelScope.launch {
            repository.deleteSubPage(subPage)
        }

        val updatedSubPagesMap = state.subPagesMap.toMutableMap()
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
                state.imagesMap,
                state.diaryPageListState
            )
        }
    }

    fun updateSubPageOffset(subPageId: String, offset: Int) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        var subPage = state.subPagesMap[subPageId] ?: return
        val pageId = state.subPagesMap.getValue(subPageId).pageId

        when(subPage.testOverflow) {
            1 -> {
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
                    resetSubPageTextCalculation(nextSubPage.id)
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
        if (subPage.testOverflow < 2) {
            val prevSubPage = getPreviousSubPage(subPageId)
            if (prevSubPage == null || prevSubPage.testOverflow == 2)
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
                updatedImageMap,
                state.diaryPageListState
            )
        }
    }

    fun updateDiaryImageTransform(diaryImageId: String, offsetX: Int, offsetY: Int, scale: Float, rotation: Float, locally: Boolean = false) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val diaryImage = state.imagesMap[diaryImageId] ?: return

        // should never be null (if we update, we loaded the image)
        val updatedDiaryImage = diaryImage.copy(offsetX = offsetX, offsetY = offsetY, scale = scale, rotation = rotation)

        updateDiaryImage(updatedDiaryImage, locally = locally)
        resetSubPageTestOverflow(diaryImage.subPageId)
    }

    fun deleteImage(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val diaryImage = state.imagesMap[diaryImageId] ?: return

        viewModelScope.launch {
            repository.deleteDiaryImage(diaryImage)
        }

        val updatedImagesMap = state.imagesMap.toMutableMap()
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
                updatedImagesMap,
                state.diaryPageListState
            )
        }
    }

    fun moveImageUp(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val diaryImage = state.imagesMap[diaryImageId] ?: return
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

        val diaryImage = state.imagesMap[diaryImageId] ?: return
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

        val diaryImage = state.imagesMap[diaryImageId] ?: return false
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val imageIndex = subPage.imageIds.indexOf(diaryImageId)
        return imageIndex == subPage.imageIds.size-1
    }

    fun isFirstSubPageImage(diaryImageId: String): Boolean {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return false

        val diaryImage = state.imagesMap[diaryImageId] ?: return false
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)

        val imageIndex = subPage.imageIds.indexOf(diaryImageId)
        return imageIndex == 0
    }

    fun isImageInLastSubPage(diaryImageId: String): Boolean {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return false

        val diaryImage = state.imagesMap[diaryImageId] ?: return false
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)
        val page = state.pagesMap.getValue(subPage.pageId)

        return page.subPageIds.last() == subPage.id
    }

    fun isImageInFirstSubPage(diaryImageId: String): Boolean {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return false

        val diaryImage = state.imagesMap[diaryImageId] ?: return false
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)
        val page = state.pagesMap.getValue(subPage.pageId)

        return page.subPageIds.first() == subPage.id
    }

    fun moveImagePrevSubPage(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val diaryImage = state.imagesMap[diaryImageId] ?: return
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)
        val page = state.pagesMap.getValue(subPage.pageId)
        val prevSubPageId = page.subPageIds[page.subPageIds.indexOf(subPage.id) - 1]
        val prevSubPage = subPagesMap.getValue(prevSubPageId)

        val updatedImage = diaryImage.copy(subPageId = prevSubPageId)
        updateDiaryImage(updatedImage)

        val updatedCurrImageIds = subPage.imageIds.toMutableList()
        updatedCurrImageIds.remove(diaryImageId)
        val updatedCurrSubPage = subPage.copy(imageIds = updatedCurrImageIds)
        updateSubPage(updatedCurrSubPage)

        val updatedPrevImageIds = prevSubPage.imageIds.toMutableList()
        updatedPrevImageIds.add(diaryImageId)
        val updatedPrevSubPage = prevSubPage.copy(imageIds = updatedPrevImageIds)
        updateSubPage(updatedPrevSubPage)
    }

    fun moveImageNextSubPage(diaryImageId: String) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val diaryImage = state.imagesMap[diaryImageId] ?: return
        val subPagesMap = state.subPagesMap
        val subPage = subPagesMap.getValue(diaryImage.subPageId)
        val page = state.pagesMap.getValue(subPage.pageId)
        val nextSubPageId = page.subPageIds[page.subPageIds.indexOf(subPage.id) + 1]
        val nextSubPage = subPagesMap.getValue(nextSubPageId)

        val updatedImage = diaryImage.copy(subPageId = nextSubPageId)
        updateDiaryImage(updatedImage)

        val updatedCurrImageIds = subPage.imageIds.toMutableList()
        updatedCurrImageIds.remove(diaryImageId)
        val updatedCurrSubPage = subPage.copy(imageIds = updatedCurrImageIds)
        updateSubPage(updatedCurrSubPage)

        val updatedNextImageIds = nextSubPage.imageIds.toMutableList()
        updatedNextImageIds.add(diaryImageId)
        val updatedPrevSubPage = nextSubPage.copy(imageIds = updatedNextImageIds)
        updateSubPage(updatedPrevSubPage)
    }

    private fun onUpdateDeletePageDialog(newDeleteDialogState: Boolean) {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        _uiState.update {
            ViewDiaryUiState.Success(
                state.diary,
                state.pagesMap,
                state.subPagesMap,
                state.imagesMap,
                state.diaryPageListState,
                newDeleteDialogState
            )
        }
    }

    fun onCloseDeletePageDialog() {
        onUpdateDeletePageDialog(false)
    }

    fun onOpenDeleteDiaryDialog() {
        onUpdateDeletePageDialog(true)
    }

    fun onDeletePage(pageId: String){
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return

        val page = state.pagesMap[pageId] ?: return

        // it's not possible to delete last page
        if (pageId == state.pages.last().id) return

        viewModelScope.launch {
            repository.deletePage(page)
        }

        val diaryPageIds = state.diary.pageIds.toMutableList()
        diaryPageIds.remove(pageId)
        val diary = state.diary.copy(pageIds = diaryPageIds)

        val subPagesMap = state.subPagesMap.toMutableMap()
        val imagesMap = state.imagesMap.toMutableMap()

        val pagesMap = state.pagesMap.toMutableMap()
        pagesMap.getValue(pageId).subPageIds.forEach { subPageId ->
            subPagesMap.getValue(subPageId).imageIds.forEach { imageId ->
                imagesMap.remove(imageId)
            }
            subPagesMap.remove(subPageId)
        }
        pagesMap.remove(pageId)

        _uiState.update {
            ViewDiaryUiState.Success(
                diary,
                pagesMap,
                subPagesMap,
                imagesMap,
                state.diaryPageListState,
                false
            )
        }
    }

    fun getSubPagesToRender(): List<DiarySubPage> {
        val state = uiState.value
        if (state !is ViewDiaryUiState.Success) return listOf()

        return state.subPages.filter{ subPage -> subPage.testOverflow < 2 }
    }
}