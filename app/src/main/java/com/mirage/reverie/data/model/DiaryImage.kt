package com.mirage.reverie.data.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset

data class DiaryImage(
    val id: String,
    val subPageId: String,
    val subPagePosition: Int,
    var offset: Offset,
    val bitmap: Bitmap
)
