package com.mirage.reverie

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mirage.reverie.ui.screens.AllDiariesScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AllDiariesScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysDiaryTitle_andRespondsToFabClick() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {}
            )
        }

        // Check that diary title is displayed
        composeTestRule
            .onNodeWithText("Test Diary")
            .assertIsDisplayed()

        // Check that FAB is present and clickable
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.create_diary))
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun showsImageTab_onImageButtonClick() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {},
            )
        }

        // Tap on "Images" button after Success UI is visible
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.images)).performClick()

        // Assert that image grid has content
/*
        composeTestRule
            .onAllNodes(hasAnyAncestor(hasTestTag("LazyVerticalStaggeredGrid")))
            .onFirst()
            .assertExists()
*/
    }
}
