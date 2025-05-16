package com.mirage.reverie.data.model

// DiaryState contains all the data of the diary
data class Diary(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val cover: String = "",
    val pageIds: List<String> = listOf()
)
