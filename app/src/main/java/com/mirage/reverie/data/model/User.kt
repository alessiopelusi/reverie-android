package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @get:Exclude val id: String = "",
    val email: String = "",
    val username: String = "",
    val name: String = "",
    val surname: String = "",
    val diaryIds: List<String> = listOf(),
    val sentTimeCapsuleIds: List<String> = listOf(),
    val receivedTimeCapsuleIds: List<String> = listOf()
): Parcelable {
    // shadows Parcelable stability attribute
    @Exclude
    fun getStability(): Int {
        return this.getStability()
    }
}

