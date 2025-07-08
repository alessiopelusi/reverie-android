package com.mirage.reverie.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude

@Keep
data class Email (
    @get:Exclude val email: String = "",
    val uid: String = ""
)
