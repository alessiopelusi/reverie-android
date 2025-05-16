package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

// Routes (Diary is the root)
@Serializable
data class DiaryRoute(val id: String)

@Serializable
data class ViewDiaryRoute(val diaryId: String)

@Serializable
data class EditDiaryPageRoute(val pageId: String)
