package com.mirage.reverie.data.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json

data class DiaryImage(
    @Json(ignore = true) val id: String,
    val subPageId: String,
    val subPagePosition: Int,
    var offset: Offset,
    val bitmap: Bitmap
) : SerializableDataClass()
