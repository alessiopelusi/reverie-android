package com.mirage.reverie.data.model

import android.os.Parcelable
import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @Json(ignore = true) val id: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val diaryIds: List<String> = listOf()
) : SerializableDataClass(), Parcelable

