package com.mirage.reverie.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude

@Keep
data class DiarySubPage(
    @get:Exclude val id: String = "",
    val pageId: String = "",
    val diaryId: String = "",
    @get:Exclude val contentEndIndex: Int = 0,
    @get:Exclude val refreshCounter: Int = 0,
    @get:Exclude val testOverflow: Int = 0,
    val imageIds: List<String> = listOf()
)
