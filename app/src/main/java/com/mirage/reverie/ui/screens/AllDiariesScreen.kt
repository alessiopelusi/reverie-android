package com.mirage.reverie.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.ui.theme.PaperColor
import com.mirage.reverie.ui.theme.Purple80
import com.mirage.reverie.viewmodel.AllDiariesUiState
import com.mirage.reverie.viewmodel.AllDiariesViewModel
import com.mirage.reverie.viewmodel.ButtonState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.mirage.reverie.R
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.ui.components.ConfirmDelete

@Composable
fun AllDiariesScreen(
    updatedDiary: Diary? = null,
    updatedImages: List<DiaryImage>? = null,
    onNavigateToDiary: (String) -> Unit,
    onNavigateToEditDiary: (String) -> Unit,
    onNavigateToCreateDiary: () -> Unit,
    viewModel: AllDiariesViewModel = hiltViewModel()
) {
    viewModel.overwriteDiary(updatedDiary)
    viewModel.overwriteImages(updatedImages)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is AllDiariesUiState.Loading -> CircularProgressIndicator()
        is AllDiariesUiState.Success -> {
            val diaries = (uiState as AllDiariesUiState.Success).diaries
            val currentPage = (uiState as AllDiariesUiState.Success).currentPage
            val pagerState = (uiState as AllDiariesUiState.Success).pagerState
            val diaryCoversMap = (uiState as AllDiariesUiState.Success).diaryCoversMap

            val currentDiary = diaries[currentPage]

            val buttonElements = (uiState as AllDiariesUiState.Success).buttonElements
            val buttonState = (uiState as AllDiariesUiState.Success).buttonState

            val deleteDialogState = (uiState as AllDiariesUiState.Success).deleteDialogState

            LazyVerticalStaggeredGrid(
                modifier = Modifier
                    .fillMaxSize(),
//                    .border(width = 2.dp, color = Color.Magenta, shape = RectangleShape),
                columns = StaggeredGridCells.Fixed(3),
            ) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Row(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(diaries.size) { iteration ->
                            val color =
                                if (currentPage == iteration) Color.DarkGray else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(12.dp)
                            )
                        }
                    }
                }
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    val pageInteractionSource = remember { MutableInteractionSource() }

                    HorizontalPager(
//                        modifier = Modifier
//                            .border(width = 2.dp, color = Color.Red, shape = RectangleShape),
                        contentPadding = PaddingValues(horizontal = 35.dp),
                        state = pagerState
                    ) { absolutePage ->
                        val relativePage = absolutePage % diaries.size
                        Card(
                            Modifier
                                .padding(8.dp)
                                .border(width = 2.dp, color = Color.Black)
//                                .graphicsLayer {
//                                    // Calculate the absolute offset for the current page from the
//                                    // scroll position. We use the absolute value which allows us to mirror
//                                    // any effects for both directions
//                                    val pageOffset = (
//                                            (currentPage - relativePage) + pagerState
//                                                .currentPageOffsetFraction
//                                            ).absoluteValue
//
//                                    // We animate the alpha, between 50% and 100%
//                                    alpha = lerp(
//                                        start = 0.5f,
//                                        stop = 1f,
//                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
//                                    )
//
//                                    scaleX = lerp(
//                                        start = 0.9f,
//                                        stop = 1f,
//                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
//                                    )
//                                    scaleY = lerp(
//                                        start = 0.9f,
//                                        stop = 1f,
//                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
//                                    )
//                                }
                                .clickable(
                                    interactionSource = pageInteractionSource,
                                    indication = LocalIndication.current
                                ) {
                                    onNavigateToDiary(currentDiary.id)
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp), // Adds spacing between elements
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {


                                DiaryCoverComposable(
                                    modifier = Modifier,
                                    coverUrl = diaryCoversMap.getValue(currentDiary.coverId).url
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    text = currentDiary.title
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = currentDiary.description
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    IconButton (
                                        // replace pagerState.currentPage with the actual id of the currentPage diary
//                                                onClick = { onNavigateToEditDiary(currentDiary.id) },
                                        onClick = {
                                            onNavigateToEditDiary(currentDiary.id)
                                        },
                                        colors = IconButtonColors(
                                            containerColor = PaperColor,
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            disabledContentColor = MaterialTheme.colorScheme.primary
                                        ),
//                                                modifier = Modifier
//                                                    .align(Alignment.Bottom),
                                    ) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(
                                        onClick = viewModel::onOpenDeleteDiaryDialog,
                                        colors = IconButtonColors(
                                            containerColor = Color.White,
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            disabledContentColor = MaterialTheme.colorScheme.primary
                                        ),
//                                                modifier = Modifier
//                                                    .align(Alignment.Bottom),
                                    ) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val cornerRadius = 16.dp

                        buttonElements.forEachIndexed { index, item ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.onButtonStateUpdate(item)
                                },
//                                modifier = when (index) {
//                                    0 ->
//                                        Modifier
//                                            .offset(0.dp, 0.dp)
//                                            .zIndex(if (buttonState == item) 1f else 0f)
//
//                                    else ->
//                                        Modifier
//                                            .offset((-1 * index).dp, 0.dp)
//                                            .zIndex(if (buttonState == item) 1f else 0f)
//                                },
                                shape = when (index) {
                                    0 -> RoundedCornerShape(
                                        topStart = cornerRadius,
                                        topEnd = 0.dp,
                                        bottomStart = cornerRadius,
                                        bottomEnd = 0.dp
                                    )

                                    buttonElements.size - 1 -> RoundedCornerShape(
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
                                    1.dp, if (buttonState == item) {
                                        Purple80
                                    } else {
                                        Purple80.copy(alpha = 0.75f)
                                    }
                                ),
                                colors = if (buttonState == item) {
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
                                Text(item.toString())
                            }

                        }
                    }
                }
                when(buttonState) {
                    ButtonState.IMAGES -> {
                        val diaryImages = (uiState as AllDiariesUiState.Success).diaryPhotosMap[currentDiary.id]

                        if (diaryImages != null) {
                            items(diaryImages) { image ->
                                AsyncImage(
                                    model = image.url,
                                    contentScale = ContentScale.Crop,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            }
                        }
                    }
                    ButtonState.VIDEOS -> {

                    }
                    ButtonState.TEXTS -> {
                        item{
                            Text("pages: ${currentDiary.pageIds.size}")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Padding for spacing from screen edges
                contentAlignment = Alignment.BottomEnd // Align content to bottom-right
            ) {
                FloatingActionButton(
                    onClick = onNavigateToCreateDiary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, "Small floating action button.")
                }
            }

            if (deleteDialogState) {
                ConfirmDelete (
                    stringResource(R.string.confirm_diary_deletion),
                    stringResource(R.string.delete_diary),
                    viewModel::onCloseDeleteDiaryDialog
                ) {
                    viewModel.onDeleteDiary(
                        currentDiary.id,
                    )
                }
            }
        }
        is AllDiariesUiState.Error -> Text(text = "Error: ${(uiState as AllDiariesUiState.Error).exception.message}")
    }
}

@Composable
fun DiaryCoverComposable(modifier: Modifier, coverUrl: String) {
    Box(
        modifier = modifier
//            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null
        )
    }
}


/*@Composable
fun DiaryContentBar(
    viewModel: AllDiariesViewModel = hiltViewModel()
){
    var selectedView by remember { mutableStateOf(ViewType.INFO) }
}*/