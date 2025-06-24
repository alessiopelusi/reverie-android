package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable

@Serializable
object AllTimeCapsulesRoute

@Serializable
data class ViewTimeCapsuleRoute(val timeCapsuleId: String)

@Serializable
object CreateTimeCapsuleRoute