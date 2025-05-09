package com.example.reverie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.example.reverie.ui.theme.Purple80
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlin.math.absoluteValue


@Serializable
data class EditDiary(val diaryId: Int)

@Serializable
data class AllDiariesParent(val profileId: Int)

@Serializable
data class AllDiaries(val profileId: Int)


// DiaryState contains all the data of the diary
data class AllDiariesState(
    val profileId: Int,
    val diaries: List<StateFlow<DiaryState>>,
    val pagerState: PagerState = PagerState(
        // endlessPagerMultiplier = 1000
        // endlessPagerMultiplier/2 = 500
        // offset = 1
        pageCount = {diaries.size*1000},
        currentPage = diaries.size*500 + 1
    ),
) {
    val currentPage: Int
        get() = pagerState.currentPage % diaries.size
}

// HiltViewModel inject SavedStateHandle + other dependencies provided by AppModule
@HiltViewModel
class AllDiariesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: DiaryRepository
) : ViewModel() {

    private val allDiaries = savedStateHandle.toRoute<AllDiaries>()
    // Expose screen UI state
    private val _uiState = MutableStateFlow(AllDiariesState(
        allDiaries.profileId,
        repository.getAllProfileDiaries(allDiaries.profileId)
    ))
    val uiState: StateFlow<AllDiariesState> = _uiState.asStateFlow()
}

@Composable
fun AllDiariesScreen(onNavigateToDiary: (Int) -> Unit, onNavigateToEditDiary: (Int) -> Unit,  viewModel: AllDiariesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val diariesStates = uiState.diaries.map{it.collectAsStateWithLifecycle()}

    Column(
        modifier = Modifier
            .fillMaxSize().border(width = 2.dp, color = Color.Magenta, shape = RectangleShape)
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pageInteractionSource = remember { MutableInteractionSource() }

        Text(
            modifier = Modifier.padding(8.dp),
            text = diariesStates[uiState.currentPage].value.title
        )

        HorizontalPager(
            modifier = Modifier
                .border(width = 2.dp, color = Color.Red, shape = RectangleShape)
                .weight(1f, false),
            contentPadding = PaddingValues(50.dp),
            state = uiState.pagerState
        ) { absolutePage ->
            val relativePage = absolutePage % diariesStates.size
            Card (
                Modifier
                    .padding(8.dp)
                    .graphicsLayer {
                        // Calculate the absolute offset for the current page from the
                        // scroll position. We use the absolute value which allows us to mirror
                        // any effects for both directions
                        val pageOffset = (
                                (uiState.currentPage - relativePage) + uiState.pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // We animate the alpha, between 50% and 100%
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        scaleX = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.9f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .clickable(
                        interactionSource = pageInteractionSource,
                        indication = LocalIndication.current
                    ) {
                        onNavigateToDiary(uiState.diaries[uiState.currentPage].value.id)
                    }
            ) {
                DiaryPage(
                    modifier = Modifier.fillMaxSize(),
                    text = diariesStates[uiState.currentPage].value.cover
                )
            }
        }

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(uiState.diaries.size) { iteration ->
                val color = if (uiState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }

        Button(
            // replace pagerState.currentPage with the actual id of the currentPage diary
            onClick = { onNavigateToEditDiary(uiState.diaries[uiState.currentPage].value.id) },
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

        val itemsList: List<String> = (1..5).map { "It $it" }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val cornerRadius = 16.dp
            var selectedIndex by remember { mutableIntStateOf(-1) }

            itemsList.forEachIndexed { index, item ->
                OutlinedButton (
                    onClick = { selectedIndex = index },
                    modifier = when (index) {
                        0 ->
                            Modifier
                                .offset(0.dp, 0.dp)
                                .zIndex(if (selectedIndex == index) 1f else 0f)
                        else ->
                            Modifier
                                .offset((-1 * index).dp, 0.dp)
                                .zIndex(if (selectedIndex == index) 1f else 0f)
                    },
                    shape = when (index) {
                        0 -> RoundedCornerShape(
                            topStart = cornerRadius,
                            topEnd = 0.dp,
                            bottomStart = cornerRadius,
                            bottomEnd = 0.dp
                        )
                        itemsList.size - 1 -> RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = cornerRadius,
                            bottomStart = 0.dp,
                            bottomEnd = cornerRadius
                        )
                        else -> RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    },
                    border = BorderStroke(
                        1.dp, if (selectedIndex == index) {
                            Purple80
                        } else {
                            Purple80.copy(alpha = 0.75f)
                        }
                    ),
                    colors = if (selectedIndex == index) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Purple80.copy(alpha = 0.1f),
                            contentColor = Purple80
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = Purple80
                        )
                    }
                ) {
                    Text(item)
                }
            }
        }
    }

}
