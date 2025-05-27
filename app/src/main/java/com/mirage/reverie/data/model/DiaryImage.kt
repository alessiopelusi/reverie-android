package com.mirage.reverie.data.model

import android.graphics.Bitmap
import com.google.firebase.firestore.Exclude
import androidx.core.graphics.createBitmap

data class DiaryImage(
    @get:Exclude val id: String = "",
    val subPageId: String = "",
    val diaryId: String = "",
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val url: String = "",
    @get:Exclude val bitmap: Bitmap = createBitmap(1, 1, Bitmap.Config.ALPHA_8)
)
