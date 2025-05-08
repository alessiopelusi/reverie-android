package com.example.reverie


// example of Database connection
interface ApiService {
    fun getDiaryById(id: Int): DiaryState

    companion object {
        fun create(): ApiService {
            // Simulazione di un'implementazione reale
            return object : ApiService {
                override fun getDiaryById(id: Int): DiaryState {
                    return DiaryState(id, "Titolo del diario", "Contenuto del diario")
                }
            }
        }
    }
}