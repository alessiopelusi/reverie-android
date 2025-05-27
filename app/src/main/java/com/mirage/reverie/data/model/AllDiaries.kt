package com.mirage.reverie.data.model

// DiaryState contains all the data of the diary
data class AllDiaries(
    val userId: String,
    val diaryIds: List<String>
)
