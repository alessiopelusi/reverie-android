package com.mirage.reverie

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romainguy.graphics.path.toPath
import dev.romainguy.text.combobreaker.FlowType
import dev.romainguy.text.combobreaker.TextFlowJustification
import dev.romainguy.text.combobreaker.material3.TextFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.min
import kotlin.random.Random


// Routes (Diary is the root)
@Serializable
data class Diary(val diaryId: Int)

@Serializable
data class ViewDiary(val diaryId: Int)

@Serializable
data class EditDiaryPage(val pageId: Int)


data class DiaryPageState(
    val id: Int = 0,
    val pageNumber: Int,
    val content: String = "",
    val subPages: List<DiarySubPageState> = listOf(DiarySubPageState(Random.nextInt(Int.MAX_VALUE), id, 0, content.length)),
)

data class DiarySubPageState(
    val id: Int,
    val pageId: Int,
    val position: Int,
    var contentEndIndex: Int,
    var cipolla: Int = 0,
    val testOverflow: Int = 0,
    val images: List<DiaryImage> = listOf(DiaryImage(pageId, 0, Offset.Zero, drawableToBitmap(ReverieApp.instance, R.drawable.ic_launcher_background)))
)

data class DiaryImage(
    val subPageId: Int,
    val subPagePosition: Int,
    var imageOffset: Offset,
    val bitmap: Bitmap
)

// DiaryState contains all the data of the diary
data class DiaryState(
    val id: Int = 0,
    val profileId: Int = 0,
    val title: String = "",
    val cover: String = "",
    val pages: List<DiaryPageState> = listOf()
) {
    val subPages: List<DiarySubPageState>
        get() = pages.flatMap { it.subPages }
}


// Using Hilt we inject a dependency (apiSevice)
@Singleton
class DiaryRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val _diaries = mutableMapOf<Int, MutableStateFlow<DiaryState>>()
    private val _pages = mutableMapOf<Int, MutableStateFlow<DiaryPageState>>()

    fun getDiaryById(diaryId: Int): StateFlow<DiaryState> {
        if (diaryId !in _diaries) {
            val diary = apiService.getDiaryById(diaryId)
            _diaries[diaryId] = MutableStateFlow(diary)
        }
        return _diaries.getValue(diaryId).asStateFlow()
    }

    fun updateDiary(diary: DiaryState) {
        if (diary.id in _diaries) _diaries.getValue(diary.id).update { diary }
        // update database
    }

    // TODO improve retrieval
    fun getAllProfileDiaries(profileId: Int): List<StateFlow<DiaryState>> {
        // We fetch only the missing diaries
        val profileDiaries = apiService.getAllProfileDiaries(
            profileId,
            _diaries.filter{ it.value.value.profileId == profileId }.map{ it.key}
        )
        _diaries.putAll(profileDiaries.associateBy(
            { diary -> diary.id },
            { diary -> MutableStateFlow(diary) })
        )
        return _diaries.filter{ it.value.value.profileId == profileId }.map{ it.value.asStateFlow() }
    }

    fun getPageById(pageId: Int): StateFlow<DiaryPageState> {
        if (pageId !in _pages) {
            val page = apiService.getPageById(pageId)
            _pages[pageId] = MutableStateFlow(page)
        }
        return _pages.getValue(pageId).asStateFlow()
    }

    fun updateDiaryPage(page: DiaryPageState) {
        if (page.id in _pages) _pages.getValue(page.id).update { page }
    }
}


// Using Hilt we inject a dependency (apiSevice)
@Singleton
class DiaryPagesRepository @Inject constructor(
    private val apiService: ApiService
) {

    private val _pages = mutableMapOf<Int, MutableStateFlow<DiaryPageState>>()

    fun getPageById(pageId: Int): StateFlow<DiaryPageState> {
        if (pageId !in _pages) {
            val page = apiService.getPageById(pageId)
            _pages[pageId] = MutableStateFlow(page)
        }
        return _pages.getValue(pageId).asStateFlow()
    }

    fun updateDiaryPage(page: DiaryPageState) {
        if (page.id in _pages) _pages.getValue(page.id).update { page }
    }
}


// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository
) : ViewModel() {

    private val diary = savedStateHandle.toRoute<Diary>()
    // Expose screen UI state
/*
    val uiState : StateFlow<DiaryState> = repository.getDiaryById(diary.diaryId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = repository.getDiaryById(diary.diaryId).value // Wrong?
    )
*/
    private val _uiState = MutableStateFlow(repository.getDiaryById(diary.diaryId).value)
    val uiState = _uiState.asStateFlow()

    // Handle business logic
    fun changeTitle(newTitle: String) {
        viewModelScope.launch {
            val diary = uiState.value.copy(title = newTitle)
            repository.updateDiary(diary)
        }
    }

    fun addSubPage(pageId: Int, newSubPage: DiarySubPageState) {
        viewModelScope.launch {
            val pages = uiState.value.pages.map { page ->
                if (page.id == pageId) {
                    page.copy(subPages = page.subPages + newSubPage) // Create a new list
                } else {
                    page
                }
            }
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            repository.updateDiary(updatedDiary)
        }
    }

    fun editSubPage(subPageId: Int, newSubPage: DiarySubPageState) {
        viewModelScope.launch {
            // Aggiorna la lista delle pagine
            val pages = uiState.value.pages.map { page ->
                page.copy(
                    subPages = page.subPages.map { subPage ->
                        if (subPage.id == subPageId) {
                            newSubPage // Sostituisce il subPage con l'id corrispondente
                        } else {
                            subPage
                        }
                    }
                )
            }
            // Aggiorna il DiaryState
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            // Salva nel repository
            repository.updateDiary(updatedDiary)
        }
    }

    fun editSubPageContentEndIndex(subPageId: Int, contentEndIndex: Int) {
        viewModelScope.launch {
            // Aggiorna la lista delle pagine
            val pages = uiState.value.pages.map { page ->
                page.copy(
                    subPages = page.subPages.map { subPage ->
                        if (subPage.id == subPageId) {
                            subPage.copy(contentEndIndex = contentEndIndex, cipolla = subPage.cipolla+1)
                        } else {
                            subPage
                        }
                    }
                )
            }
            // Aggiorna il DiaryState
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            // Salva nel repository
            repository.updateDiary(updatedDiary)
        }
    }

    fun incrementSubPageCipolla(subPageId: Int) {
        viewModelScope.launch {
            // Aggiorna la lista delle pagine
            val pages = uiState.value.pages.map { page ->
                page.copy(
                    subPages = page.subPages.map { subPage ->
                        if (subPage.id == subPageId) {
                            subPage.copy(cipolla = subPage.cipolla+1)
                        } else {
                            subPage
                        }
                    }
                )
            }
            // Aggiorna il DiaryState
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            // Salva nel repository
            repository.updateDiary(updatedDiary)
        }
    }

    fun incrementSubPageTestOverflow(subPageId: Int) {
        viewModelScope.launch {
            // Aggiorna la lista delle pagine
            val pages = uiState.value.pages.map { page ->
                page.copy(
                    subPages = page.subPages.map { subPage ->
                        if (subPage.id == subPageId) {
                            subPage.copy(testOverflow = subPage.testOverflow+1)
                        } else {
                            subPage
                        }
                    }
                )
            }
            // Aggiorna il DiaryState
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            // Salva nel repository
            repository.updateDiary(updatedDiary)
        }
    }

    fun resetSubPageTestOverflow(subPageId: Int) {
        viewModelScope.launch {
            // Aggiorna la lista delle pagine
            val pages = uiState.value.pages.map { page ->
                page.copy(
                    subPages = page.subPages.map { subPage ->
                        if (subPage.id == subPageId) {
                            subPage.copy(testOverflow = 0, cipolla = subPage.cipolla+1)
                        } else {
                            subPage
                        }
                    }
                )
            }
            // Aggiorna il DiaryState
            val updatedDiary = uiState.value.copy(pages = pages)
            _uiState.update { updatedDiary }
            // Salva nel repository
            repository.updateDiary(updatedDiary)
        }
    }
}

@Composable
fun ViewDiaryScreen(onNavigateToEditDiaryPage: (Int) -> Unit, viewModel: DiaryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(width = 2.dp, color = Color.Magenta, shape = RectangleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = uiState.title
        )

        val diaryPageListState = rememberLazyListState()
        // start lazyrow from the end
        LaunchedEffect(Unit) {
            diaryPageListState.scrollToItem(uiState.subPages.lastIndex)
        }

        BoxWithConstraints (
            modifier = Modifier.border(width = 2.dp, color = Color.Red, shape = RectangleShape).weight(1f, false),
        ) {
            val boxWithConstraintsScope = this
            LazyRow (
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                state = diaryPageListState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = diaryPageListState)
            ) {
                itemsIndexed(uiState.subPages) { index, item ->
                    Layout(
                        content = {
                            // Here's the content of each list item.
                            val widthFraction = 0.90f
                            uiState.pages.find { it.id == item.pageId }?.let { page ->
                                DiaryPage(
                                    modifier = Modifier
                                        .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                        .aspectRatio(9f/16f),
                                    page,
                                    item.position,
                                    viewModel
                                )
                            }
                        },
                        measurePolicy = { measurables, constraints ->
                            // I'm assuming you'll declaring just one root
                            // composable in the content function above
                            // so it's measuring just the Box
                            val placeable = measurables.first().measure(constraints)
                            // maxWidth is from the BoxWithConstraints
                            val maxWidthInPx = boxWithConstraintsScope.maxWidth.roundToPx()
                            // Box width
                            val itemWidth = placeable.width
                            // Calculating the space for the first and last item
                            val startSpace =
                                if (index == 0) (maxWidthInPx - itemWidth) / 2 else 0
                            val endSpace =
                                if (index == uiState.subPages.lastIndex) (maxWidthInPx - itemWidth) / 2 else 0
                            // The width of the box + extra space
                            val width = startSpace + placeable.width + endSpace
                            layout(width, placeable.height) {
                                // Placing the Box in the right X position
                                val x = if (index == 0) startSpace else 0
                                placeable.place(x, 0)
                            }
                        }
                    )
                }
            }
        }

        Text(
            modifier = Modifier.padding(8.dp),
            text = "Page 1/1",
        )

        Button(
            onClick = { onNavigateToEditDiaryPage(diaryPageListState.firstVisibleItemIndex) },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
fun EditDiaryScreen(viewModel: DiaryViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Text(
        modifier = Modifier.padding(8.dp),
        text = "You are editing your diary!",
    )
    EditTitleTextField(uiState.title, onUpdateTitle = { viewModel.changeTitle(it) })
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class EditDiaryPageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryPagesRepository
) : ViewModel() {

    private val diary = savedStateHandle.toRoute<EditDiaryPage>()
    // Expose screen UI state
    val uiState : StateFlow<DiaryPageState> = repository.getPageById(diary.pageId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = repository.getPageById(diary.pageId).value // Wrong?a
    )

    // Handle business logic
    fun changeContent(newContent: String) {
        viewModelScope.launch {
            val diary = uiState.value.copy(content = newContent)
            repository.updateDiaryPage(diary)
        }
    }
}


@Composable
fun EditDiaryPageScreen(viewModel: EditDiaryPageViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Text(
        modifier = Modifier.padding(8.dp),
        text = "You are editing your diary!",
    )
    EditContentTextField(uiState.content, onUpdateContent = { newContent -> viewModel.changeContent(newContent) })
}

@Composable
fun EditTitleTextField(title: String, onUpdateTitle: (String) -> Unit) {
    TextField(
        value = title,
        onValueChange = onUpdateTitle,
        label = { Text("Titolo") }
    )
}

@Composable
fun EditContentTextField(title: String, onUpdateContent: (String) -> Unit) {
    TextField(
        value = title,
        onValueChange = onUpdateContent,
        label = { Text("Contenuto") }
    )
}

@Composable
fun DiaryPage(modifier: Modifier, page: DiaryPageState, subPageIndex: Int, viewModel: DiaryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val textStyle = LocalTextStyle.current.merge(
        TextStyle(color = colorScheme.onSurface, fontSize = 40.sp)
    )
    var sampleText by remember { mutableStateOf("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris condimentum vestibulum tellus, id porta ante efficitur at. Vivamus congue aliquam est, id venenatis dolor ultricies sed. Sed ut erat egestas, porttitor odio id, ultrices purus. Etiam eu ante nec lacus dictum porta. Donec ut vestibulum massa. Donec aliquam ut tellus ac gravida. Nullam finibus semper ante. Etiam a tincidunt lectus. Quisque nec pulvinar libero, non maximus nunc. Curabitur porta tempus augue ac rhoncus. Cras euismod ligula nisl, ullamcorper semper neque vehicula nulla.") }

    var startIndex = if(subPageIndex!=0) page.subPages[subPageIndex-1].contentEndIndex else 0
    if (startIndex == sampleText.length) startIndex = 0
    if (startIndex > page.subPages[subPageIndex].contentEndIndex) {
        viewModel.editSubPageContentEndIndex(page.subPages[subPageIndex].id, startIndex + 1)
        // TODO:hack
        page.subPages[subPageIndex].contentEndIndex = startIndex+1
    }

    //Log.d("TextFlowLayoutResult before", "${page.id} $subPageIndex $startIndex ${page.subPages[subPageIndex].contentEndIndex} ${page.subPages[subPageIndex].id} $testOverflow")

    TextFlow(
        sampleText.substring(startIndex = startIndex, sampleText.length),
        modifier = modifier
            .fillMaxSize(),
        style = textStyle,
        justification = TextFlowJustification.Auto,
        columns = 1,
        onTextFlowLayoutResult = { textFlowLayoutResult ->
            //Log.d("TextFlowLayoutResult", "${textFlowLayoutResult.lastOffset} ${sampleText.length}")
            val lastOffset = textFlowLayoutResult.lastOffset
            if (page.subPages[subPageIndex].testOverflow == 0) {
                viewModel.editSubPageContentEndIndex(page.subPages[subPageIndex].id, sampleText.length)
                // TODO:hack
                page.subPages[subPageIndex].contentEndIndex = sampleText.length
                viewModel.incrementSubPageTestOverflow(page.subPages[subPageIndex].id)
            } else if (page.subPages[subPageIndex].testOverflow == 1) {
                viewModel.incrementSubPageTestOverflow(page.subPages[subPageIndex].id)
            } else if (page.subPages[subPageIndex].testOverflow == 2) {
                viewModel.editSubPageContentEndIndex(page.subPages[subPageIndex].id, min(startIndex + lastOffset, sampleText.length))
                // TODO:hack
                page.subPages[subPageIndex].contentEndIndex = startIndex + lastOffset
                Log.d("TextFlowLayoutResult", "${page.id} $subPageIndex $startIndex ${page.subPages[subPageIndex].contentEndIndex} ${page.subPages[subPageIndex].id}")
                // if not last page
                if (subPageIndex+1 < page.subPages.size) {
                    viewModel.editSubPageContentEndIndex(page.subPages[subPageIndex+1].id, sampleText.length)
                    viewModel.resetSubPageTestOverflow(page.subPages[subPageIndex+1].id)
                } else if (page.subPages[subPageIndex].contentEndIndex < sampleText.length) {
                    Log.d("DiaryPage", "add subpage")
                    viewModel.addSubPage(page.id, DiarySubPageState(Random.nextInt(Int.MAX_VALUE), page.id, subPageIndex+1, sampleText.length))
                }
                viewModel.incrementSubPageTestOverflow(page.subPages[subPageIndex].id)
            }
        },
    ) {
        Text(
            uiState.pages.find { it.id == page.id }!!.subPages[subPageIndex].cipolla.toString(),
            color = Color.Transparent,
            modifier = Modifier.size(0.2f.dp).align(Alignment.BottomEnd)
        )
        page.subPages[subPageIndex].images.forEach {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    //.fillMaxSize()
                    .offset { IntOffset(it.imageOffset.x.toInt(), it.imageOffset.y.toInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures (
                            onDrag = { change, dragAmount ->
                                change.consume()
                                it.imageOffset += dragAmount
                                viewModel.resetSubPageTestOverflow(page.subPages[subPageIndex].id)
                            },
                        )
                    }
                    .flowShape(FlowType.Outside, 0.dp, it.bitmap.toPath(0.5f).asComposePath()),
                bitmap = it.bitmap.asImageBitmap(),
                contentDescription = ""
            )
        }
    }
}

