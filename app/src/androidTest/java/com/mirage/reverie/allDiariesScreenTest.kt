package com.mirage.reverie

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.mirage.reverie.data.model.Diary
import com.mirage.reverie.data.model.DiaryCover
import com.mirage.reverie.ui.screens.AllDiariesScreen
import com.mirage.reverie.viewmodel.AllDiariesUiState
import com.mirage.reverie.viewmodel.AllDiariesViewModel
import com.mirage.reverie.viewmodel.ButtonState
import com.mirage.reverie.viewmodel.createPagerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class AllDiariesScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allDiariesScreen_displaysTitleAndDescription() {
        // Creo i dati di test
        val testDiary = Diary(
            id = "diary1",
            title = "Titolo Diario Test",
            description = "Descrizione Diario Test",
            coverId = "cover1",
            creationDate = Timestamp.now(),
            pageIds = listOf("page1", "page2")
        )
        val testDiaryCover = DiaryCover(
            id = "cover1",
            url = "https://example.com/cover1.jpg"
        )

        val uiState = AllDiariesUiState.Success(
            diariesIds = listOf(testDiary.id),
            diariesMap = mapOf(testDiary.id to testDiary),
            diaryCoversMap = mapOf(testDiary.coverId to testDiaryCover),
            diaryPhotosMap = mapOf(testDiary.id to emptyList()),
            pagerState = createPagerState(1),
            buttonState = ButtonState.INFO,
            deleteDialogState = false
        )

        // Mock del ViewModel
        val mockViewModel = mock(AllDiariesViewModel::class.java)

        // Quando viene chiamato uiState, ritorna un flow con il nostro stato di test
        runBlocking {
            `when`(mockViewModel.uiState).thenReturn(flowOf(uiState) as StateFlow<AllDiariesUiState>?)
        }

        // Override dei metodi usati (vuoti per il test)
        doNothing().`when`(mockViewModel).overwriteDiary(anyOrNull())
        doNothing().`when`(mockViewModel).overwriteImages(anyOrNull())

        // Composable sotto test
        composeTestRule.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {},
                viewModel = mockViewModel
            )
        }

        // Verifico la presenza dei testi
        composeTestRule.onNodeWithText(testDiary.title).assertIsDisplayed()
        composeTestRule.onNodeWithText(testDiary.description).assertIsDisplayed()
    }
}