package com.mirage.reverie.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mirage.reverie.R
import com.mirage.reverie.data.model.DiaryPage
import com.mirage.reverie.viewmodel.ViewDiaryUiState
import com.mirage.reverie.viewmodel.ViewDiaryViewModel
import dev.romainguy.graphics.path.toPath
import dev.romainguy.text.combobreaker.FlowType
import dev.romainguy.text.combobreaker.TextFlowJustification
import dev.romainguy.text.combobreaker.material3.TextFlow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.mirage.reverie.data.model.DiaryImage
import com.mirage.reverie.formatDate
import com.mirage.reverie.ui.components.ConfirmDelete
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ViewDiaryScreen(
    onNavigateToEditDiaryPage: (String) -> Unit,
    updatedPage: DiaryPage? = null,
    onComplete: (List<DiaryImage>) -> Unit,
    viewModel: ViewDiaryViewModel = hiltViewModel()
) {
    // used when we send back page from editDiaryPage, we avoid another database call updating manually
    viewModel.overwritePage(updatedPage)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back press
    BackHandler {
        // Add data to SavedStateHandle before navigating back
        onComplete((uiState as ViewDiaryUiState.Success).images)

        // Perform back navigation
    }

    when (uiState) {
        is ViewDiaryUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is ViewDiaryUiState.Success -> {
            val diary = (uiState as ViewDiaryUiState.Success).diary
            val pages = (uiState as ViewDiaryUiState.Success).pages
            val pagesMap = (uiState as ViewDiaryUiState.Success).pagesMap
            val subPages = (uiState as ViewDiaryUiState.Success).subPages
            val subPagesMap = (uiState as ViewDiaryUiState.Success).subPagesMap
            val deleteDialogState = (uiState as ViewDiaryUiState.Success).deleteDialogState

            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = diary.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                val diaryPageListState = (uiState as ViewDiaryUiState.Success).diaryPageListState

                val currentSubPageIndex by remember {
                    derivedStateOf {
                        val layoutInfo = diaryPageListState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) return@derivedStateOf 0

                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2

                        // Find the item whose center is closest to the viewport center
                        visibleItems.minByOrNull { item ->
                            val itemCenter = item.offset + item.size / 2
                            abs(itemCenter - viewportCenter)
                        }?.index ?: 0
                    }
                }
                val currentSubPage = subPages.getOrNull(currentSubPageIndex) ?: subPages[0]
                val currentPage = pagesMap.getValue(currentSubPage.pageId)
                val currentPageIndex = pages.indexOf(currentPage)
                // start lazyrow from the end
                /*LaunchedEffect(Unit) {
                    diaryPageListState.scrollToItem(subPages.lastIndex)
                }*/

                BoxWithConstraints (
                    modifier = Modifier
                        .weight(1f, false)
                        .padding(vertical = 10.dp)
                ) {
                    val boxWithConstraintsScope = this

                    // TODO: big big hack to load everything and update end indices
                    viewModel.getSubPagesToRender().forEachIndexed{ index, subPage ->
                        Layout(
                            content = {
                                // Here's the content of each list item.
                                val widthFraction = 0.90f
                                DiaryPage(
                                    modifier = Modifier
                                        .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                        .aspectRatio(9f / 16f)
                                        .fillMaxSize()
                                        .padding(horizontal = 5.dp, vertical = 10.dp)
                                    ,
                                    subPage.id,
                                    viewModel,
                                    transparent = true
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

                    LazyRow (
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = diaryPageListState,
                        flingBehavior = rememberSnapFlingBehavior(lazyListState = diaryPageListState)
                    ) {
                        itemsIndexed(subPages) { index, subPage ->
                            Layout(
                                content = {
                                    // Here's the content of each list item.
                                    val widthFraction = 0.90f
                                    val diaryPaperWhite = Color(0xFFFFFBF0)
                                    Card (
                                        colors = CardDefaults.cardColors(containerColor = diaryPaperWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ){
                                        DiaryPage(
                                            modifier = Modifier
                                                .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                                .aspectRatio(9f / 16f)
                                                .fillMaxSize()
                                                .padding(horizontal = 5.dp, vertical = 10.dp),
                                            subPage.id,
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
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(currentPage.date),
                        )
                        val isLastPage = currentPage == pages.last()
                        IconButton(
                            onClick = viewModel::onOpenDeleteDiaryDialog,
                            enabled = !isLastPage,
                            colors = IconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = if (isLastPage) Color.Transparent else colorScheme.primary,
                                disabledContainerColor = colorScheme.primaryContainer,
                                disabledContentColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (deleteDialogState) {
                            ConfirmDelete(
                                stringResource(R.string.confirm_page_deletion),
                                stringResource(R.string.delete_page),
                                viewModel::onCloseDeletePageDialog
                            ) {
                                viewModel.onDeletePage(
                                    currentPage.id,
                                )

                                // go to the first subpage of previous page to avoid issues
                                val prevPage = pages[currentPageIndex-1]
                                val index = subPages.indexOf(subPagesMap.getValue(prevPage.subPageIds.first()))
                                coroutineScope.launch {
                                    diaryPageListState.animateScrollToItem(index)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onNavigateToEditDiaryPage(currentPage.id)

                                // go to the first subpage of page to avoid issues
                                val index = subPages.indexOf(subPagesMap.getValue(currentPage.subPageIds.first()))
                                coroutineScope.launch {
                                    diaryPageListState.animateScrollToItem(index)
                                }
                            },
                            colors = ButtonColors(
                                containerColor = colorScheme.secondary,
                                contentColor = colorScheme.primary,
                                disabledContainerColor = colorScheme.primaryContainer,
                                disabledContentColor = colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
//                            modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scrivi"
                                )
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                            }

                        }
                        val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
                            if (uri != null) {
                                viewModel.uploadImage(uri, currentSubPage.id)
                            }
                        }
                        Button(
                            onClick = {
                                // Launch the photo picker and let the user choose only images.
                                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonColors(
                                containerColor = colorScheme.secondary,
                                contentColor = colorScheme.primary,
                                disabledContainerColor = colorScheme.primaryContainer,
                                disabledContentColor = colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
//                            modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Inserisci foto"
                                )
                                Icon(Icons.Outlined.Camera, contentDescription = stringResource(R.string.select_image), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

            }
        }
        is ViewDiaryUiState.Error -> Text(text = "Error: ${(uiState as ViewDiaryUiState.Error).exception.message}")
    }

}

@Composable
fun DiaryPage(modifier: Modifier, subPageId: String, viewModel: ViewDiaryViewModel, transparent: Boolean = false) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val subPagesMap = (uiState as ViewDiaryUiState.Success).subPagesMap
    val subPagesImagesMap = (uiState as ViewDiaryUiState.Success).subPageImagesMap
    val subPage = subPagesMap.getValue(subPageId)

    val textStyle = LocalTextStyle.current.merge(
        TextStyle(color = if (transparent) Color.Transparent else colorScheme.onSurface, fontSize = 18.sp)
    )

    BoxWithConstraints(
        modifier = modifier,
    ) {
        var isContextMenuVisible by rememberSaveable { mutableStateOf(false) }
        var contextMenuImageId by rememberSaveable { mutableStateOf("") }
        var pressOffset by remember { mutableStateOf(DpOffset.Zero) }

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
                if (transparent) {
                    viewModel.updateSubPageOffset(
                        subPageId,
                        textFlowLayoutResult.lastOffset
                    )
                }
                // switch based on testOverflow state
            },
        ) {
            // workaround to update textflow when changing contentEndIndex
            if (!transparent) {
                Text(
                    subPage.refreshCounter.toString(),
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(0.2f.dp)
                        .align(Alignment.BottomEnd)
                )
            }

            val subPageImages = subPagesImagesMap.getValue(subPageId)
            if (transparent) {
                val exampleBitmap =
                    drawableToBitmap(LocalContext.current, R.drawable.ic_launcher_background)
                // workaround to update textflow when there is no image
                if (subPageImages.isEmpty()) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .flowShape(
                                FlowType.None,
                                0.dp,
                                exampleBitmap.toPath(0.5f).asComposePath()
                            ),
                        bitmap = exampleBitmap.asImageBitmap(),
                        contentDescription = "",
                        alpha = 0f
                    )
                }
            }
            subPageImages.forEach { currentImage ->
                // necessary to prevent block on pointerInput and to pass the actual updated value
                val image by rememberUpdatedState(currentImage)

                var lastUpdateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
                var updated by remember { mutableStateOf(true) }

                if (!transparent) {
                    LaunchedEffect(updated) {
                        while (true) {
                            val currentTime = System.currentTimeMillis()
                            if (!updated && currentTime - lastUpdateTime > 300) {
                                viewModel.updateDiaryImage(image)
                                updated = true
                            }
                            delay(100) // Small delay to avoid busy looping
                        }
                    }
                }

                val matrix = androidx.compose.ui.graphics.Matrix()
                matrix.scale(image.scale, image.scale)
                matrix.rotateZ(image.rotation)

                val path = currentImage.bitmap.toPath().asComposePath()
                path.transform(matrix)

                Image(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset { IntOffset(image.offsetX, image.offsetY) }
                        .graphicsLayer(
                            scaleX = image.scale,
                            scaleY = image.scale,
                            rotationZ = image.rotation,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (!transparent) {
                                        isContextMenuVisible = true
                                        contextMenuImageId = image.id
                                        pressOffset = DpOffset(
                                            it.x.toDp() + image.offsetX.toDp(),
                                            it.y.toDp() + image.offsetY.toDp()
                                        )
                                    }
                                }
                            )
                        }
                        .pointerInput(currentImage.bitmap) {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                if(!transparent) {
                                    // new scale
                                    var newScale = image.scale * zoom

                                    // newRotation
                                    val newRotation = image.rotation + rotation

                                    // Rotation in radians
                                    val rotationRad = Math.toRadians(newRotation.toDouble())

                                    val cosTheta = abs(cos(rotationRad)).toFloat()
                                    val sinTheta = abs(sin(rotationRad)).toFloat()

                                    // Adjust pan to rotation space
                                    val adjustedPanX =
                                        (pan.x * cos(rotationRad) - pan.y * sin(rotationRad)).toFloat()
                                    val adjustedPanY =
                                        (pan.x * sin(rotationRad) + pan.y * cos(rotationRad)).toFloat()


                                    val originalWidth = currentImage.bitmap.width
                                    val originalHeight = currentImage.bitmap.height

                                    var scaledWidth = originalWidth * newScale
                                    var scaledHeight = originalHeight * newScale

                                    var scaledRotatedWidth =
                                        scaledWidth * cosTheta + scaledHeight * sinTheta
                                    var scaledRotateHeight =
                                        scaledWidth * sinTheta + scaledHeight * cosTheta

                                    // Calculate half difference for padding similar to your scale logic
                                    var halfWidthDiff = abs(originalWidth - scaledRotatedWidth) / 2
                                    var halfHeightDiff =
                                        abs(originalHeight - scaledRotateHeight) / 2


                                    // Divide pan by scale to compensate for zoom level
                                    var correctedPanX = adjustedPanX * newScale
                                    var correctedPanY = adjustedPanY * newScale

                                    while (halfWidthDiff > (parentWidth - originalWidth - halfWidthDiff) || halfHeightDiff > (parentHeight - originalHeight - halfHeightDiff)) {
                                        // TODO: bad hack. While scaling or rotating, if the image size is too big the app would crash. We reduce the scale until it fits.
                                        newScale = (newScale * 0.99).toFloat()

                                        // recalculate everything with old scale
                                        scaledWidth = originalWidth * newScale
                                        scaledHeight = originalHeight * newScale

                                        scaledRotatedWidth =
                                            scaledWidth * cosTheta + scaledHeight * sinTheta
                                        scaledRotateHeight =
                                            scaledWidth * sinTheta + scaledHeight * cosTheta

                                        // Calculate half difference for padding similar to your scale logic
                                        halfWidthDiff = abs(originalWidth - scaledRotatedWidth) / 2
                                        halfHeightDiff =
                                            abs(originalHeight - scaledRotateHeight) / 2


                                        // Divide pan by scale to compensate for zoom level
                                        correctedPanX = adjustedPanX * newScale
                                        correctedPanY = adjustedPanY * newScale
                                    }

                                    val newX = (image.offsetX + correctedPanX)
                                        .coerceIn(
                                            halfWidthDiff,
                                            parentWidth - originalWidth - halfWidthDiff
                                        )
                                        .toInt()

                                    val newY = (image.offsetY + correctedPanY)
                                        .coerceIn(
                                            halfHeightDiff,
                                            parentHeight - originalHeight - halfHeightDiff
                                        )
                                        .toInt()

                                    viewModel.updateDiaryImageTransform(
                                        diaryImageId = image.id,
                                        offsetX = newX,
                                        offsetY = newY,
                                        scale = newScale,
                                        rotation = newRotation,
                                        locally = true
                                    )

                                    lastUpdateTime = System.currentTimeMillis()
                                    updated = false
                                }
                            }
                        }
                        .flowShape(FlowType.Outside, 0.dp, path),
                    bitmap = currentImage.bitmap.asImageBitmap(),
                    contentDescription = "",
                    alpha = if (transparent) 0f else 1f
                )
            }
        }

        if (!transparent) {
            data class DropDownItem(
                val text: String,
                val onClick: (String) -> Unit
            )

            val dropdownItems = listOfNotNull(
                DropDownItem(stringResource(R.string.delete), viewModel::deleteImage),
                if (!viewModel.isLastSubPageImage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_up), viewModel::moveImageUp) else null,
                if (!viewModel.isFirstSubPageImage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_down), viewModel::moveImageDown) else null,
                if (!viewModel.isImageInFirstSubPage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_prev_subpage), viewModel::moveImagePrevSubPage) else null,
                if (!viewModel.isImageInLastSubPage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_next_subpage), viewModel::moveImageNextSubPage) else null,
            )
            DropdownMenu(
                expanded = isContextMenuVisible,
                onDismissRequest = {
                    isContextMenuVisible = false
                },
                offset = pressOffset
            ) {
                dropdownItems.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            item.onClick(contextMenuImageId)
                            isContextMenuVisible = false
                        },
                        text = {
                            Text(
                                item.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        }
    }
}

fun drawableToBitmap(context: Context, drawableResId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
        ?: throw IllegalArgumentException("Drawable not found for resource ID: $drawableResId")

    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)


    return bitmap
}

