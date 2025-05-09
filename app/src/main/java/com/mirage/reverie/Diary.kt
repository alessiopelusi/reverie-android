package com.mirage.reverie

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mirage.reverie.ui.theme.PaperColor
import dagger.hilt.android.lifecycle.HiltViewModel
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
)

// DiaryState contains all the data of the diary
data class DiaryState(
    val id: Int = 0,
    val profileId: Int = 0,
    val title: String = "",
    val cover: String = "",
    val pages: List<DiaryPageState> = listOf()
)


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
    val uiState : StateFlow<DiaryState> = repository.getDiaryById(diary.diaryId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = repository.getDiaryById(diary.diaryId).value // Wrong?
    )

    // Handle business logic
    fun changeTitle(newTitle: String) {
        viewModelScope.launch {
            val diary = uiState.value.copy(title = newTitle)
            repository.updateDiary(diary)
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

        val listOfItems = uiState.pages
        val diaryPageListState = rememberLazyListState()
        // start lazyrow from the end
        LaunchedEffect(Unit) {
            diaryPageListState.scrollToItem(listOfItems.lastIndex)
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
                itemsIndexed(listOfItems) { index, item ->
                    Layout(
                        content = {
                            // Here's the content of each list item.
                            val widthFraction = 0.90f
                            DiaryPage(modifier = Modifier
                                .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                .aspectRatio(9f/16f),
                                item.content)
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
                                if (index == listOfItems.lastIndex) (maxWidthInPx - itemWidth) / 2 else 0
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
fun DiaryPage(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .background(PaperColor)
    ) {
        Text(text = text)
    }
}

