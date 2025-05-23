package com.mirage.reverie.data.model

import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json

data class DiarySubPage(
    @Json(ignore = true) val id: String = "",
    val pageId: String = "",
    val diaryId: String = "",
    @Json(ignore = true) val contentEndIndex: Int = 0,
    @Json(ignore = true) val cipolla: Int = 0,
    @Json(ignore = true) val testOverflow: Int = 0,
    val imageIds: List<String> = listOf()
) : SerializableDataClass()
