package com.mirage.reverie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mirage.reverie.R
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.ui.components.ButtonBar
import com.mirage.reverie.ui.components.ConfirmDelete
import java.text.SimpleDateFormat

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
                    span = StaggeredGridItemSpan.FullLine,

                ) {
                    val pageInteractionSource = remember { MutableInteractionSource() }
                    Column(
                        modifier = Modifier.padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalPager(
//                            contentPadding = PaddingValues(horizontal = 35.dp),
                            state = pagerState
                        ) { absolutePage ->
                            val relativePage = absolutePage % diaries.size
                            Card(
                                Modifier
                                    .padding(horizontal = 40.dp, vertical = 8.dp)
//                                    .border(width = 2.dp, color = Color.Black)
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
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
                                    if (!currentDiary.description.isEmpty()){
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            text = currentDiary.description
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
                                            // replace pagerState.currentPage with the actual id of the currentPage diary
//                                                onClick = { onNavigateToEditDiary(currentDiary.id) },
                                            onClick = {
                                                onNavigateToEditDiary(currentDiary.id)
                                            },
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                disabledContentColor = MaterialTheme.colorScheme.primary
                                            ),
//                                            modifier = Modifier.border(
//                                                BorderStroke(1.dp, Color.Black),
//                                                shape = RoundedCornerShape(
//                                                    topStart = 8.dp,
//                                                    topEnd = 8.dp,
//                                                    bottomStart = 8.dp,
//                                                    bottomEnd = 8.dp
//                                                )
//                                            ),
                                        ) {
//                                            Text(
//                                                text = "Edit",
//                                                style = TextStyle(
//                                                    fontWeight = FontWeight.Normal
//                                                )
//                                            )
//                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            // replace pagerState.currentPage with the actual id of the currentPage diary
//                                                onClick = { onNavigateToEditDiary(currentDiary.id) },
                                            onClick = viewModel::onOpenDeleteDiaryDialog,
                                            colors = IconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                disabledContentColor = MaterialTheme.colorScheme.primary
                                            ),
//                                            modifier = Modifier.border(
//                                                BorderStroke(1.dp, Color.Black),
//                                                shape = RoundedCornerShape(
//                                                    topStart = 8.dp,
//                                                    topEnd = 8.dp,
//                                                    bottomStart = 8.dp,
//                                                    bottomEnd = 8.dp
//                                                )
//                                            ),
                                        ) {
//                                            Text(
//                                                text = "Delete",
//                                                style = TextStyle(
//                                                    fontWeight = FontWeight.Normal
//                                                )
//                                            )
//                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete",
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
                    ButtonBar(buttonState, buttonElements, viewModel::onButtonStateUpdate)
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
                        item(
                            span = StaggeredGridItemSpan.FullLine
                        ){
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                            ){
                                val formatter = SimpleDateFormat("dd MMMM yyyy")
                                Text(
                                    text = "Data di creazione:",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                )
                                Text(
                                    text = formatter.format(currentDiary.creationDate.toDate()),
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
                                    text = "Numero di pagine:",
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