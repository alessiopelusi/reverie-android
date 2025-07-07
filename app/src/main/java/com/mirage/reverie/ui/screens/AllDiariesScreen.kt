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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.mirage.reverie.viewmodel.AllDiariesUiState
import com.mirage.reverie.viewmodel.AllDiariesViewModel
import com.mirage.reverie.viewmodel.ButtonState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mirage.reverie.R
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.formatDate
import com.mirage.reverie.ui.components.ButtonBar
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
        is AllDiariesUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                columns = StaggeredGridCells.Fixed(3),
            ) {
                item(
                    span = StaggeredGridItemSpan.FullLine,

                ) {
                    val pageInteractionSource = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier.padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
                            state = pagerState
                        ) { diaryNumber ->
                            val diary = diaries[diaryNumber%diaries.size]

                            Card(
                                Modifier
                                    .padding(horizontal = 40.dp, vertical = 8.dp)
                                    .clickable(
                                        interactionSource = pageInteractionSource,
                                        indication = LocalIndication.current
                                    ) {
                                        onNavigateToDiary(diary.id)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp), // Adds spacing between elements
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    DiaryCoverComposable(
                                        modifier = Modifier,
                                        coverUrl = diaryCoversMap.getValue(diary.coverId).url
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        text = diary.title
                                    )
                                    if (diary.description.isNotEmpty()){
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            text = diary.description
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ){
                                        IconButton(
                                            onClick = {
                                                onNavigateToEditDiary(diary.id)
                                            },
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                disabledContentColor = MaterialTheme.colorScheme.primary
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = stringResource(R.string.edit_diary),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = viewModel::onOpenDeleteDiaryDialog,
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                disabledContentColor = MaterialTheme.colorScheme.primary
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = stringResource(R.string.delete),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(diaries.size) { iteration ->
                                val color = if (currentPage == iteration) Color.Black else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .border(
                                            BorderStroke(1.dp, Color.Black),
                                            shape = CircleShape
                                        )
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                }
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    ButtonBar(buttonState, buttonElements, viewModel::onButtonStateUpdate) { item ->
                        when(item) {
                            ButtonState.INFO -> stringResource(R.string.info)
                            else -> stringResource(R.string.images)
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
                                    contentDescription = stringResource(R.string.image),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            }
                        }
                    }
//                    ButtonState.VIDEOS -> {
//
//                    }
                    ButtonState.INFO -> {
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ){
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                            ){
                                Text(
                                    text = stringResource(R.string.creation_date) + ":",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                )
                                Text(
                                    text = formatDate(currentDiary.creationDate.toDate()),
                                )
                            }
                        }
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ){
                            HorizontalDivider(thickness = 1.dp)
                        }
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ){
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                            ){
                                Text(
                                    text = stringResource(R.string.page_number) + ":",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                )
                                Text(
                                    text = currentDiary.pageIds.size.toString(),
                                )
                            }
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
                    Icon(Icons.Filled.Add, stringResource(R.string.create_diary))
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
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null
        )
    }
}
