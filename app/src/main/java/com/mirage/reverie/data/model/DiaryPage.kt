package com.mirage.reverie.data.model

import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json

data class DiaryPage(
    @Json(ignore = true) val id: String = "",
    val diaryId: String = "",
    val pageNumber: Int = 0,
    val content: String = "",
    val subPageIds: List<String> = listOf()
) : SerializableDataClass()
