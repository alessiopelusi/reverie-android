package com.mirage.reverie.data.model

import com.google.firebase.firestore.Exclude

data class DiarySubPage(
    @get:Exclude val id: String = "",
    val pageId: String = "",
    val diaryId: String = "",
    @get:Exclude val contentEndIndex: Int = 0,
    @get:Exclude val cipolla: Int = 0,
    @get:Exclude val testOverflow: Int = 0,
    val imageIds: List<String> = listOf()
)
