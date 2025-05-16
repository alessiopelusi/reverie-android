package com.mirage.reverie.data.model

data class DiaryPage(
    val id: String = "",
    val diaryId: String = "",
    val pageNumber: Int = 0,
    val content: String = "",
    val subPageIds: List<String> = listOf()
)
