package com.mirage.reverie.data.model

import com.google.firebase.firestore.Exclude

data class Username (
    @get:Exclude val username: String = "",
    val uid: String = ""
)
