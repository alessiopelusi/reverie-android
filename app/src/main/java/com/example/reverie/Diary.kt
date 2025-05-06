package com.example.reverie

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reverie.ui.theme.PaperColor
import kotlinx.serialization.Serializable


@Serializable object Diary

@Composable
fun DiaryScreen(navController: NavController) {
    val title = "Emotional Diary"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(width = 2.dp, color = Color.Magenta, shape = RectangleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = title
        )

        val listOfItems: List<String> = (1..10).map { "Item $it" }
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
                                item)
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
            onClick = {
                navController.navigate(ModifyDiary)
            },
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
fun DiaryPage(modifier: Modifier, text: String) {
    Box(
        modifier = modifier
            .border(width = 2.dp, color = Color.Blue, shape = RectangleShape)
            .background(PaperColor)
    ) {
        Text(text = text)
    }
}

