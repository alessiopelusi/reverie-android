package com.mirage.reverie.data.model

import androidx.compose.ui.geometry.Offset
import com.mirage.reverie.R
import com.mirage.reverie.ReverieApp
import com.mirage.reverie.drawableToBitmap
import kotlin.random.Random

data class DiarySubPage(
    val id: String = "",
    val pageId: String = "",
    var contentEndIndex: Int = 0,
    var cipolla: Int = 0,
    val testOverflow: Int = 0,
    val imageIds: List<String> = listOf()
)
