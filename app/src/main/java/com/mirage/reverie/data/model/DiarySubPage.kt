package com.mirage.reverie.data.model

import com.google.firebase.firestore.Exclude

data class DiarySubPage(
    @Exclude val id: String = "",
    val pageId: String = "",
    val diaryId: String = "",
    @Exclude val contentEndIndex: Int = 0,
    @Exclude val cipolla: Int = 0,
    @Exclude val testOverflow: Int = 0,
    val imageIds: List<String> = listOf()
)
