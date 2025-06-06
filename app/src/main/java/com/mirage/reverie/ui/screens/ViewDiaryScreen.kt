package com.mirage.reverie.ui.screens

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.mirage.reverie.drawableToBitmap
import com.mirage.reverie.viewmodel.ViewDiaryUiState
import com.mirage.reverie.viewmodel.ViewDiaryViewModel
import dev.romainguy.graphics.path.toPath
import dev.romainguy.text.combobreaker.FlowType
import dev.romainguy.text.combobreaker.TextFlowJustification
import dev.romainguy.text.combobreaker.material3.TextFlow
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.romainguy.graphics.path.toPaths
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ViewDiaryScreen(
    onNavigateToEditDiaryPage: (String) -> Unit,
    updatedPage: DiaryPage? = null,
    viewModel: ViewDiaryViewModel = hiltViewModel()
) {
    // used when we send back page from editDiaryPage, we avoid another database call updating manually
    viewModel.overwritePage(updatedPage)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is ViewDiaryUiState.Loading -> CircularProgressIndicator()
        is ViewDiaryUiState.Success -> {
            val diary = (uiState as ViewDiaryUiState.Success).diary
            val pages = (uiState as ViewDiaryUiState.Success).pages
            val pagesMap = (uiState as ViewDiaryUiState.Success).pagesMap
            val subPages = (uiState as ViewDiaryUiState.Success).subPages

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

                val diaryPageListState = rememberLazyListState(initialFirstVisibleItemIndex = subPages.lastIndex)
                val currentSubPageIndex by remember {
                    derivedStateOf {
                        val layoutInfo = diaryPageListState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) return@derivedStateOf 0

                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2

                        // Find the item whose center is closest to the viewport center
                        visibleItems.minByOrNull { item ->
                            val itemCenter = item.offset + item.size / 2
                            kotlin.math.abs(itemCenter - viewportCenter)
                        }?.index ?: 0
                    }
                }
                val currentSubPage = subPages[currentSubPageIndex]
                val currentPage = pagesMap.getValue(currentSubPage.pageId)
                // start lazyrow from the end
                /*LaunchedEffect(Unit) {
                    diaryPageListState.scrollToItem(subPages.lastIndex)
                }*/

                BoxWithConstraints (
                    modifier = Modifier
                        .border(width = 2.dp, color = Color.Red, shape = RectangleShape)
                        .weight(1f, false),
                ) {
                    val boxWithConstraintsScope = this

                    // TODO: big big hack to load everything and update end indices
                    subPages.forEachIndexed{ index, subPage ->
                        Layout(
                            content = {
                                // Here's the content of each list item.
                                val widthFraction = 0.90f
                                DiaryPage(
                                    modifier = Modifier
                                        .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                        .aspectRatio(9f / 16f),
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
                                    DiaryPage(
                                        modifier = Modifier
                                            .widthIn(max = LocalWindowInfo.current.containerSize.width.dp * widthFraction)
                                            .aspectRatio(9f / 16f),
                                        subPage.id,
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
                    text = "${stringResource(R.string.day)} ${currentPage.date.format(DateTimeFormatter.ofPattern("dd MM YYYY"))}",
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "${stringResource(R.string.page)} ${pages.indexOf(currentPage) + 1}/${pages.size}",
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "${stringResource(R.string.sub_page)} ${currentPage.subPageIds.indexOf(currentSubPage.id) + 1}/${currentPage.subPageIds.size}",
                )

                // Registers a photo picker activity launcher in single-select mode.
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
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Outlined.Camera, contentDescription = "Camera")
                }


                Button(
                    onClick = { onNavigateToEditDiaryPage(currentPage.id) },
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
        TextStyle(color = if (transparent) Color.Transparent else colorScheme.onSurface, fontSize = 40.sp)
    )

    /*viewModel.incrementSubPageCipolla(subPageId)*/

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        var isContextMenuVisible by rememberSaveable {
            mutableStateOf(false)
        }
        var contextMenuImageId by rememberSaveable {
            mutableStateOf("")
        }
        var pressOffset by remember {
            mutableStateOf(DpOffset.Zero)
        }
        /*var itemHeight by remember {
            mutableStateOf(0.dp)
        }
        val density = LocalDensity.current*/
        data class DropDownItem(
            val text: String,
            val onClick: (String) -> Unit
        )
        val dropdownItems = listOfNotNull(
            DropDownItem(stringResource(R.string.delete), viewModel::deleteImage),
            if (!viewModel.isLastSubPageImage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_up), viewModel::moveImageUp) else null,
            if (!viewModel.isFirstSubPageImage(contextMenuImageId)) DropDownItem(stringResource(R.string.move_down), viewModel::moveImageDown) else null,
        )

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
            // workaround to update textflow when changin contentEndIndex
            Text(
                subPage.cipolla.toString(),
                color = Color.Transparent,
                modifier = Modifier
                    .size(0.2f.dp)
                    .align(Alignment.BottomEnd)
            )

            val exampleBitmap = drawableToBitmap(LocalContext.current, R.drawable.ic_launcher_background)
            // workaround to update textflow when there is no image
            val subPageImages = subPagesImagesMap.getValue(subPageId)
            if (subPageImages.isEmpty()) {
                Image(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        //.fillMaxSize()
                        .flowShape(FlowType.None, 0.dp, exampleBitmap.toPath(0.5f).asComposePath()),
                    bitmap = exampleBitmap.asImageBitmap(),
                    contentDescription = "",
                    alpha = 0f
                )
            }
            subPageImages.forEach { currentImage ->
                // necessary to prevent block on pointerInput and to pass the actual updated value
                val image by rememberUpdatedState(currentImage)

                var lastUpdateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
                var updated by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    while (true) {
                        val currentTime = System.currentTimeMillis()
                        if (!updated && currentTime - lastUpdateTime > 300) {
                            viewModel.updateDiaryImage(image)
                            updated = true
                        }
                        delay(100) // Small delay to avoid busy looping
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
                        //.fillMaxSize()
                        .offset { IntOffset(image.offsetX, image.offsetY) }
                        .graphicsLayer(
                            scaleX = image.scale,
                            scaleY = image.scale,
                            rotationZ = image.rotation,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    isContextMenuVisible = true
                                    contextMenuImageId = image.id
                                    pressOffset = DpOffset(it.x.toDp() + image.offsetX.toDp(), it.y.toDp() + image.offsetY.toDp())
                                }
                            )
                        }
                        .pointerInput(currentImage.bitmap) {
                            detectTransformGestures { centroid, pan, zoom, rotation ->
                                // new scale
                                var newScale = image.scale * zoom

                                // newRotation
                                val newRotation = image.rotation + rotation

                                // Rotation in radians
                                val rotationRad = Math.toRadians(newRotation.toDouble())

                                val cosTheta = abs(cos(rotationRad)).toFloat()
                                val sinTheta = abs(sin(rotationRad)).toFloat()

                                // Adjust pan to rotation space
                                val adjustedPanX = (pan.x * cos(rotationRad) - pan.y * sin(rotationRad)).toFloat()
                                val adjustedPanY = (pan.x * sin(rotationRad) + pan.y * cos(rotationRad)).toFloat()


                                val originalWidth = currentImage.bitmap.width
                                val originalHeight = currentImage.bitmap.height

                                var scaledWidth = originalWidth * newScale
                                var scaledHeight = originalHeight * newScale

                                var scaledRotatedWidth = scaledWidth * cosTheta + scaledHeight * sinTheta
                                var scaledRotateHeight = scaledWidth * sinTheta + scaledHeight * cosTheta

                                // Calculate half difference for padding similar to your scale logic
                                var halfWidthDiff = abs(originalWidth - scaledRotatedWidth) / 2
                                var halfHeightDiff = abs(originalHeight - scaledRotateHeight) / 2


                                // Divide pan by scale to compensate for zoom level
                                var correctedPanX = adjustedPanX * newScale
                                var correctedPanY = adjustedPanY * newScale

                                while (halfWidthDiff > (parentWidth - originalWidth - halfWidthDiff) || halfHeightDiff > (parentHeight - originalHeight - halfHeightDiff)) {
                                    // TODO: bad hack. While scaling or rotating, if the image size is too big the app would crash. We reduce the scale until it fits.
                                    newScale = (newScale * 0.99).toFloat()

                                    // recalculate everything with old scale
                                    scaledWidth = originalWidth * newScale
                                    scaledHeight = originalHeight * newScale

                                    scaledRotatedWidth = scaledWidth * cosTheta + scaledHeight * sinTheta
                                    scaledRotateHeight = scaledWidth * sinTheta + scaledHeight * cosTheta

                                    // Calculate half difference for padding similar to your scale logic
                                    halfWidthDiff = abs(originalWidth - scaledRotatedWidth) / 2
                                    halfHeightDiff = abs(originalHeight - scaledRotateHeight) / 2


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
                                viewModel.resetSubPageTestOverflow(subPageId)

                                lastUpdateTime = System.currentTimeMillis()
                                updated = false
                            }
                        }
                        /*.onSizeChanged {
                            itemHeight = with(density) { it.height.toDp() }
                        }*/
                        .flowShape(FlowType.Outside, 0.dp, path),
                    bitmap = currentImage.bitmap.asImageBitmap(),
                    contentDescription = "",
                    alpha = if (transparent) 0f else 1f
                )
            }
        }
        DropdownMenu (
            expanded = isContextMenuVisible,
            onDismissRequest = {
                isContextMenuVisible = false
            },
            offset = pressOffset
                /*.copy(
                y = pressOffset.y - itemHeight
            )*/
        ) {
            dropdownItems.forEach { item ->
                DropdownMenuItem(onClick = {
                    item.onClick(contextMenuImageId)
                    isContextMenuVisible = false
                }) {
                    Text(text = item.text)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The ")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            " important. Please grant all of them for the app to function properly."
        } else {
            " denied. The app cannot function without them."
        }
    )
    return textToShow.toString()
}