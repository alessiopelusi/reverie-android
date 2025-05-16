package com.mirage.reverie

import android.graphics.Bitmap
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
import androidx.compose.runtime.remember
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
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romainguy.graphics.path.toPath
import dev.romainguy.text.combobreaker.FlowType
import dev.romainguy.text.combobreaker.TextFlowJustification
import dev.romainguy.text.combobreaker.material3.TextFlow
import javax.inject.Inject
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


// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class EditDiaryPageViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryPagesRepository
) : ViewModel() {

    private val diary = savedStateHandle.toRoute<EditDiaryPageRoute>()
    // Expose screen UI state
    val uiState : StateFlow<DiaryPage> = repository.getPageById(diary.pageId).stateIn(
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
fun EditDiaryScreen(viewModel: DiaryViewModel = hiltViewModel()){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Text(
        modifier = Modifier.padding(8.dp),
        text = "You are editing your diary!",
    )
    EditTitleTextField(uiState.title, onUpdateTitle = { viewModel.changeTitle(it) })
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

