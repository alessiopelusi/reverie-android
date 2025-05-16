package com.mirage.reverie.data.model

import androidx.compose.foundation.pager.PagerState

// DiaryState contains all the data of the diary
data class AllDiaries(
    val profileId: Int,
    val diaries: List<String>,
    val pagerState: PagerState = PagerState(
        // endlessPagerMultiplier = 1000
        // endlessPagerMultiplier/2 = 500
        // offset = 1
        pageCount = {diaries.size*1000},
        currentPage = diaries.size*500 + 1
    ),
) {
    val currentPage: Int
        get() = pagerState.currentPage % diaries.size
}
