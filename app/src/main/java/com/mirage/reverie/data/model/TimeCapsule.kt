package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeCapsule (
    @get:Exclude val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val deadline: Timestamp = Timestamp.now(), // es. 2 anni

    val emails: List<String> = listOf(), // email a cui verrà spedito il link
    val phones: List<String> = listOf(), // numeri di telefono a cui verrà spedito il link
    val receiversIds: List<String> = listOf(), // utenti che hanno cliccato il link ricevuto (inizialmente è vuota)

    val creationDate: Timestamp = Timestamp.now(), // data di creazione della capsula
    // https://stackoverflow.com/questions/46406376/kotlin-class-does-not-get-its-boolean-value-from-firebase#comment79775161_46406376
    @field:JvmField val isSent: Boolean = false,             // TimeCapsule sent to emails and phones
): Parcelable {
    // shadows Parcelable stability attribute
    @Exclude
    fun getStability(): Int {
        return this.getStability()
    }
}

