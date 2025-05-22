package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable


@Serializable
data class DiaryRoute(val diaryId: String)

@Serializable
data class ViewDiaryRoute(val diaryId: String)

@Serializable
data class EditDiaryPageRoute(val pageId: String)
