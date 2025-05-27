package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.mirage.reverie.toLocalDate
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class DiaryPage(
    @get:Exclude val id: String = "",
    val diaryId: String = "",
    val content: String = "",
    val subPageIds: List<String> = listOf(),
    val timestamp: Timestamp = Timestamp.now()
): Parcelable {
    val date: LocalDate
        @Exclude get() = timestamp.toDate().toLocalDate()

    // shadows Parcelable stability attribute
    @Exclude
    fun getStability(): Int {
        return this.getStability()
    }
}
