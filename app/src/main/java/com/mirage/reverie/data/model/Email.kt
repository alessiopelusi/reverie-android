package com.mirage.reverie.data.model

import com.google.firebase.firestore.Exclude

data class Email (
    @get:Exclude val email: String = "",
    val uid: String = ""
)
