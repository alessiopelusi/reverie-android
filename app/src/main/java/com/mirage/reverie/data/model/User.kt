package com.mirage.reverie.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val diaryIds: List<String> = listOf()
)

