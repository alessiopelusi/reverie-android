package com.mirage.reverie.data.model

import com.mirage.reverie.data.SerializableDataClass
import com.squareup.moshi.Json
import kotlinx.serialization.Serializable

data class DiaryCover (
    @Json(ignore = true) val id: String = "",
    val name: String = "",
    val url: String = ""
) : SerializableDataClass()