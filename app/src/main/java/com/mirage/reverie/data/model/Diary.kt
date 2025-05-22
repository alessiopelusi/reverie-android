package com.mirage.reverie.data.model

import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json

// DiaryState contains all the data of the diary
data class Diary (
    @Json(ignore = true) val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val coverId: String = "",
    val pageIds: List<String> = listOf()
) : SerializableDataClass()