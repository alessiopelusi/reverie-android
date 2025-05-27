package com.mirage.reverie.data.model

import com.google.firebase.firestore.Exclude

data class DiaryCover (
    @Exclude val id: String = "",
    val name: String = "",
    val url: String = ""
)