package com.mirage.reverie.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude

@Keep
data class DiaryCover (
    @get:Exclude val id: String = "",
    val name: String = "",
    val url: String = ""
)