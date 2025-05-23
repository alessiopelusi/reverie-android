package com.mirage.reverie.data.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json

data class DiaryImage(
    @Json(ignore = true) val id: String = "",
    val subPageId: String = "",
    val diaryId: String = "",
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val url: String = "",
    @Json(ignore = true) val bitmap: Bitmap? = null
) : SerializableDataClass()
