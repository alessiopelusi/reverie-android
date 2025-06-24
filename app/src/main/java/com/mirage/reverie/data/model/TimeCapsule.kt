package com.mirage.reverie.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.datetime.LocalDateTime
import kotlinx.parcelize.Parcelize
import java.time.Period

@Parcelize
data class TimeCapsule (
    @get:Exclude val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val deadline: Timestamp = Timestamp.now(), // es. 2 anni

    val emails: List<String> = listOf(), // email a cui verrà spedito il link
    val phones: List<String> = listOf(), // numeri di telefono a cui verrà spedito il link
    val receivers: List<String> = listOf(), // utenti che hanno cliccato il link ricevuto (inizialmente è vuota)

    val visualizedBy: List<String> = listOf(), // utenti che hanno visualizzato la capsula del tempo
    val creationDate: Timestamp = Timestamp.now() // data di creazione della capsula
): Parcelable {
    // shadows Parcelable stability attribute
    @Exclude
    fun getStability(): Int {
        return this.getStability()
    }
}

