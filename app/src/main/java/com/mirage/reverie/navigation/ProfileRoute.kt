package com.mirage.reverie.navigation

import kotlinx.serialization.Serializable


@Serializable
data class ProfileRoute(val profileId: String)

@Serializable
data class ViewProfileRoute(val profileId: String)

@Serializable
data class EditProfileRoute(val profileId: String)
