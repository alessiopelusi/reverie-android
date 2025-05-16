package com.mirage.reverie.data.model

import androidx.compose.foundation.pager.PagerState

// DiaryState contains all the data of the diary
data class AllDiaries(
    val userId: String,
    val diaryIds: List<String>
)
