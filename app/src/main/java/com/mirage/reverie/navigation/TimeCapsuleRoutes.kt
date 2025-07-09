package com.mirage.reverie.navigation

import com.mirage.reverie.viewmodel.TimeCapsuleType
import kotlinx.serialization.Serializable

@Serializable
object TimeCapsulesRoute

@Serializable
object AllTimeCapsulesRoute

@Serializable
data class ViewTimeCapsuleRoute(val timeCapsuleId: String, val timeCapsuleType: TimeCapsuleType)

@Serializable
object CreateTimeCapsuleRoute