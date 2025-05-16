package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

@Serializable
data class EditDiaryRoute(val diaryId: String)

@Serializable
data class AllDiariesParentRoute(val userId: String)

@Serializable
data class AllDiariesRoute(val userId: String)
