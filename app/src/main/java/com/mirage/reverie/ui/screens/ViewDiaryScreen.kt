package com.mirage.reverie.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.viewmodel.DiaryUiState
import com.mirage.reverie.viewmodel.DiaryViewModel
import dev.romainguy.graphics.path.toPath
import dev.romainguy.text.combobreaker.FlowType
import dev.romainguy.text.combobreaker.TextFlowJustification
import dev.romainguy.text.combobreaker.material3.TextFlow

@Composable
fun ViewDiaryScreen(onNavigateToEditDiaryPage: (String) -> Unit, viewModel: DiaryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is DiaryUiState.Loading -> CircularProgressIndicator()
        is DiaryUiState.Success -> {
            val diary = (uiState as DiaryUiState.Success).diary
            val subPages = (uiState as DiaryUiState.Success).subPages
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = 2.dp, color = Color.Magenta, shape = RectangleShape),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = diary.title
                )

                val diaryPageListState = rememberLazyListState()
                // start lazyrow from the end
                LaunchedEffect(Unit) {
                    diaryPageListState.scrollToItem(subPages.lastIndex)
                }

                BoxWithConstraints (
                    modifier = Modifier
                        .border(width = 2.dp, color = Color.Red, shape = RectangleShape)
                        .weight(1f, false),
                ) {
                    val boxWithConstraintsScope = this
                    LazyRow (
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = diaryPageListState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = diaryPageListState)
                    ) {
                        itemsIndexed(subPages) { index, item ->
                            Layout(
                                content = {
                                    // Here's the content of each list item.
                                    val widthFraction = 0.90f
                                    DiaryPage(
                                        modifier = Modifier
                                            .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                            .aspectRatio(9f / 16f),
                                        item.id,
                                        viewModel
                                    )
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
                                        if (index == subPages.lastIndex) (maxWidthInPx - itemWidth) / 2 else 0
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
                    onClick = { onNavigateToEditDiaryPage(subPages[diaryPageListState.firstVisibleItemIndex].pageId) },
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
        is DiaryUiState.Error -> Text(text = "Error: ${(uiState as DiaryUiState.Error).exception.message}")
    }

}

@Composable
fun DiaryPage(modifier: Modifier, subPageId: String, viewModel: DiaryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val subPagesMap = (uiState as DiaryUiState.Success).subPagesMap
    val subPage = subPagesMap.getValue(subPageId)

    val textStyle = LocalTextStyle.current.merge(
        TextStyle(color = colorScheme.onSurface, fontSize = 40.sp)
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val parentWidth = constraints.maxWidth.toFloat()
        val parentHeight = constraints.maxHeight.toFloat()
        TextFlow (
            viewModel.getSubPageContent(subPageId),
            modifier = Modifier
                .fillMaxSize(),
            style = textStyle,
            justification = TextFlowJustification.Auto,
            columns = 1,
            onTextFlowLayoutResult = { textFlowLayoutResult ->
                viewModel.updateSubPageOffset(
                    subPageId,
                    textFlowLayoutResult.lastOffset
                )
                // switch based on testOverflow state
            },
        ) {
            Text(
                subPage.cipolla.toString(),
                color = Color.Transparent,
                modifier = Modifier
                    .size(0.2f.dp)
                    .align(Alignment.BottomEnd)
            )
            viewModel.getSubPageImages(subPageId).forEach { image ->
                Image(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        //.fillMaxSize()
                        .offset { IntOffset(image.offset.x.toInt(), image.offset.y.toInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    val newX = (image.offset.x + dragAmount.x)
                                        .coerceIn(0f, parentWidth - image.bitmap.width)
                                    val newY = (image.offset.y + dragAmount.y)
                                        .coerceIn(0f, parentHeight - image.bitmap.height)

                                    // Aggiorna l'offset vincolato
                                    change.consume()
                                    image.offset = Offset(newX, newY)
                                    viewModel.resetSubPageTestOverflow(subPageId)
                                },
                                onDragEnd = {
                                    viewModel.updateDiaryImageOffset(image.id, image.offset)
                                }
                            )
                        }
                        .flowShape(FlowType.Outside, 0.dp, image.bitmap.toPath(0.5f).asComposePath()),
                    bitmap = image.bitmap.asImageBitmap(),
                    contentDescription = ""
                )
            }
        }
    }
}

