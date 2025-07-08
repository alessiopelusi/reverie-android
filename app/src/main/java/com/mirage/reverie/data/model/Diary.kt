package com.mirage.reverie.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

// DiaryState contains all the data of the diary
@Keep
@Parcelize
data class Diary (
    @get:Exclude val id: String = "",
    val uid: String = "",
    val title: String = "",
    val description: String = "",
    val coverId: String = "",
    val pageIds: List<String> = listOf(),
    val creationDate: Timestamp = Timestamp.now()
) : Parcelable {
    // shadows Parcelable stability attribute
    @Exclude
    fun getStability(): Int {
        return this.getStability()
    }
}