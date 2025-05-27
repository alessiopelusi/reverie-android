package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @Exclude val id: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val diaryIds: List<String> = listOf()
): Parcelable

