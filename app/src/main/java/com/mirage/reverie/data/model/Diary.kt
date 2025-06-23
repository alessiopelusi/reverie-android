package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

// DiaryState contains all the data of the diary
@Parcelize
data class Diary (
    @get:Exclude val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val coverId: String = "",
    val pageIds: List<String> = listOf(),
    val creationDate: Timestamp = Timestamp.now()
) : Parcelable