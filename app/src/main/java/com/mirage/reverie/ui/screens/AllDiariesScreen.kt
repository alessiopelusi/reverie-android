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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.ui.theme.PaperColor
import com.mirage.reverie.ui.theme.Purple80
import com.mirage.reverie.viewmodel.AllDiariesUiState
import com.mirage.reverie.viewmodel.AllDiariesViewModel
import kotlin.math.absoluteValue


@Composable
fun AllDiariesScreen(
    updatedDiary: Diary? = null,
    onNavigateToDiary: (String) -> Unit,
    onNavigateToEditDiary: (String) -> Unit,
    viewModel: AllDiariesViewModel = hiltViewModel()
) {
    viewModel.overwriteDiary(updatedDiary)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is AllDiariesUiState.Loading -> CircularProgressIndicator()
        is AllDiariesUiState.Success -> {
            val diaries = (uiState as AllDiariesUiState.Success).diaries
            val currentPage = (uiState as AllDiariesUiState.Success).currentPage
            val pagerState = (uiState as AllDiariesUiState.Success).pagerState
            val diaryCoversMap = (uiState as AllDiariesUiState.Success).diaryCoversMap
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 2.dp, color = Color.Magenta, shape = RectangleShape)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pageInteractionSource = remember { MutableInteractionSource() }

                HorizontalPager(
                    modifier = Modifier
                        .border(width = 2.dp, color = Color.Red, shape = RectangleShape)
                        .weight(1f, false),
                    contentPadding = PaddingValues(50.dp),
                    state = pagerState
                ) { absolutePage ->
                    val relativePage = absolutePage % diaries.size
                    Card(
                        Modifier
                            .padding(8.dp)
                            .background(PaperColor)
                            .graphicsLayer {
                                // Calculate the absolute offset for the current page from the
                                // scroll position. We use the absolute value which allows us to mirror
                                // any effects for both directions
                                val pageOffset = (
                                        (currentPage - relativePage) + pagerState
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
                                onNavigateToDiary(diaries[currentPage].id)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Adds spacing between elements
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            DiaryCover(
                                modifier = Modifier,
                                coverUrl = diaryCoversMap.getValue(diaries[currentPage].coverId).url
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                text = diaries[currentPage].title
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = diaries[currentPage].description
                            )
                        }
                    }
                }

                Row(
                    Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
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
                                .size(16.dp)
                        )
                    }
                }

                Button(
                    // replace pagerState.currentPage with the actual id of the currentPage diary
                    onClick = { onNavigateToEditDiary(diaries[currentPage].id) },
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
                        OutlinedButton(
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

        is AllDiariesUiState.Error -> Text(text = "Error: ${(uiState as AllDiariesUiState.Error).exception.message}")
    }
}

@Composable
fun DiaryCover(modifier: Modifier, coverUrl: String) {
    Box(
        modifier = modifier
            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null
        )
    }
}
