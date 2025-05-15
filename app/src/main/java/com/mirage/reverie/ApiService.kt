package com.mirage.reverie

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import javax.inject.Singleton


// example of Database connection
@Singleton
interface ApiService {
    fun getPageById(pageId: Int): DiaryPageState
    fun getDiaryById(diaryId: Int): DiaryState
    fun getAllProfileDiaries(profileId: Int, excludeDiaryIds: List<Int> = listOf()): List<DiaryState>

    companion object {
        fun create(): ApiService {
            // Simulazione di un'implementazione reale
            return object : ApiService {
                override fun getPageById(pageId: Int): DiaryPageState {
                    return DiaryPageState(pageId, pageId, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris condimentum vestibulum tellus, id porta ante efficitur at. Vivamus congue aliquam est, id venenatis dolor ultricies sed. Sed ut erat egestas, porttitor odio id, ultrices purus. Etiam eu ante nec lacus dictum porta. Donec ut vestibulum massa. Donec aliquam ut tellus ac gravida. Nullam finibus semper ante. Etiam a tincidunt lectus. Quisque nec pulvinar libero, non maximus nunc. Curabitur porta tempus augue ac rhoncus. Cras euismod ligula nisl, ullamcorper semper neque vehicula nulla.")
                }

                override fun getDiaryById(diaryId: Int): DiaryState {
                    val list = mutableListOf<DiaryPageState>()
                    for (i in 0..5) {
                        list.add(getPageById(i))
                    }
                    return DiaryState(diaryId, 0, "Titolo del diario $diaryId", "Copertina del diario $diaryId", list)
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