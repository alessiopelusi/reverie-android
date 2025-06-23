package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

@Serializable
data class EditDiaryRoute(val diaryId: String)

@Serializable
object CreateDiaryRoute

@Serializable
object AllDiariesRoute

@Serializable
object DiariesRoute
