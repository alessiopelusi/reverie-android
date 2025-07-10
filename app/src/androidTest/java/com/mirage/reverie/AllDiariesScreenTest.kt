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
    fun displaysDiaryTitleAndRespondsToFabClick() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {}
            )
        }

        val fabContentDescription = composeTestRule.activity.getString(R.string.create_diary)

        composeTestRule
            .onNodeWithText("Test Diary")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription(fabContentDescription)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun showsImageTabOnImageButtonClick() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {},
            )
        }

        // Wait until the diary title is visible to ensure Success UI is loaded
        composeTestRule
            .onNodeWithText("Test Diary")
            .assertIsDisplayed()

        // Click the "Images" button
        val imagesButtonText = composeTestRule.activity.getString(R.string.images)
        composeTestRule
            .onNodeWithText(imagesButtonText)
            .assertIsDisplayed()
            .performClick()

        // Wait for the image with the expected content description to appear
        val imageContentDesc = composeTestRule.activity.getString(R.string.image)
        composeTestRule.waitUntil(
            condition = {
                composeTestRule
                    .onAllNodesWithContentDescription(imageContentDesc)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            },
            timeoutMillis = 5_000
        )

        // Assert that at least one image is displayed
        composeTestRule
            .onAllNodesWithContentDescription(imageContentDesc)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun displaysMultipleDiariesAndHandlesPagerSwipe() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {}
            )
        }

        // Assert that the first diary is shown
        composeTestRule.onNodeWithText("Test Diary").assertIsDisplayed()

        // Swipe left on the title (or any visible node in the pager)
        composeTestRule
            .onNodeWithText("Test Diary")
            .performTouchInput { swipeLeft() }

        // Assert second diary is displayed
        composeTestRule.onNodeWithText("Second Diary").assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Second Diary")
            .performTouchInput { swipeLeft() }

        // Assert second diary is displayed
        composeTestRule.onNodeWithText("Third Diary").assertIsDisplayed()
    }

    @Test
    fun switchingToInfoTabDisplaysCreationDateAndPageNumber() {
        composeTestRule.activity.setContent {
            AllDiariesScreen(
                onNavigateToDiary = {},
                onNavigateToEditDiary = {},
                onNavigateToCreateDiary = {}
            )
        }

        val infoText = composeTestRule.activity.getString(R.string.info)
        val creationDateLabel = composeTestRule.activity.getString(R.string.creation_date) + ":"
        val pageNumberLabel = composeTestRule.activity.getString(R.string.page_number) + ":"

        // Tap the Info button
        composeTestRule
            .onNodeWithText(infoText, ignoreCase = true)
            .performClick()

        // Validate labels are displayed
        composeTestRule.onNodeWithText(creationDateLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(pageNumberLabel).assertIsDisplayed()
    }
}
