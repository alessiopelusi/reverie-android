package com.example.reverie

import javax.inject.Singleton


// example of Database connection
@Singleton
interface ApiService {
    fun getDiaryById(diaryId: Int): DiaryState
    fun getAllProfileDiaries(profileId: Int): AllDiariesState

    companion object {
        fun create(): ApiService {
            // Simulazione di un'implementazione reale
            return object : ApiService {
                override fun getDiaryById(diaryId: Int): DiaryState {
                    return DiaryState(diaryId, "Titolo del diario $diaryId", "Contenuto del diario $diaryId")
                }

                override fun getAllProfileDiaries(profileId: Int): AllDiariesState {
                    val list: MutableList<DiaryState> = mutableListOf()
                    for (i in 0..2) {
                        list.add(getDiaryById(i))
                    }
                    return AllDiariesState(profileId, list)
                }
            }
        }
    }
}