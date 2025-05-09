package com.example.reverie

import javax.inject.Singleton


// example of Database connection
@Singleton
interface ApiService {
    fun getDiaryById(diaryId: Int): DiaryState
    fun getAllProfileDiaries(profileId: Int, excludeDiaryIds: List<Int> = listOf()): List<DiaryState>

    companion object {
        fun create(): ApiService {
            // Simulazione di un'implementazione reale
            return object : ApiService {
                override fun getDiaryById(diaryId: Int): DiaryState {
                    return DiaryState(diaryId, 0, "Titolo del diario $diaryId", "Copertina del diario $diaryId")
                }

                override fun getAllProfileDiaries(profileId: Int, excludeDiaryIds: List<Int>): List<DiaryState> {
                    val list: MutableList<DiaryState> = mutableListOf()
                    for (i in 0..2) {
                        if (i !in excludeDiaryIds) list.add(getDiaryById(i))
                    }
                    return list
                }
            }
        }
    }
}