package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

@Serializable
object DiariesRoute

@Serializable
data class EditDiaryRoute(val diaryId: String)

@Serializable
object CreateDiaryRoute

@Serializable
object AllDiariesRoute

@Serializable
data class DiaryRoute(val diaryId: String)

@Serializable
data class ViewDiaryRoute(val diaryId: String)

@Serializable
data class EditDiaryPageRoute(val pageId: String)
