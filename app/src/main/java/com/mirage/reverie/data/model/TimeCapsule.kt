package com.mirage.reverie.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.datetime.LocalDateTime
import java.time.Period

data class TimeCapsule (
    @get:Exclude val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "", // es. Lettera al futuro me, Cassetta dei ricordi
    val deadline: Timestamp, // es. 2 anni

    val emails: List<String> = listOf(), // email a cui verrà spedito il link
    val phones: List<String> = listOf(), // numeri di telefono a cui verrà spedito il link
    val receivers: List<String> = listOf(), // utenti che hanno cliccato il link ricevuto (inizialmente è vuota)

    val visualizedBy: List<String> = listOf(), // utenti che hanno visualizzato la capsula del tempo
    val creationDate: Timestamp = Timestamp.now() // data di creazione della capsula
)

enum class DeadlineOption(val years: Int) {
    ONE_YEAR(1),
    TWO_YEARS(2),
    THREE_YEARS(3),
    FIVE_YEARS(5),
    TEN_YEARS(10);
}