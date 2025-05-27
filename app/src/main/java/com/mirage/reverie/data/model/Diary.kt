package com.mirage.reverie.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

// DiaryState contains all the data of the diary
data class Diary (
    @Exclude val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val coverId: String = "",
    val pageIds: List<String> = listOf(),
    val creationDate: Timestamp = Timestamp.now()
)