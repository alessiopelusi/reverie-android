package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

@Serializable
data class EditDiary(val diaryId: String)

@Serializable
data class AllDiariesParent(val profileId: String)

@Serializable
data class AllDiaries(val profileId: String)
