package com.mirage.reverie.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude

@Keep
data class Username (
    @get:Exclude val username: String = "",
    val uid: String = ""
)
