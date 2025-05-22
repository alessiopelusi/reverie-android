package com.mirage.reverie.data.model

import android.os.Parcelable
import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

// DiaryState contains all the data of the diary
@Parcelize
data class Diary (
    @Json(ignore = true) val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val coverId: String = "",
    val pageIds: List<String> = listOf()
) : SerializableDataClass(), Parcelable