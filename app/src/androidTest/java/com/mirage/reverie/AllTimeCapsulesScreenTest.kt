package com.mirage.reverie

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mirage.reverie.ui.screens.AllTimeCapsulesScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AllTimeCapsulesScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysScheduledCapsules_byDefault() {
        composeTestRule.activity.setContent {
            AllTimeCapsulesScreen(
                onNavigateToViewTimeCapsule = {_, _ -> },
                onNavigateToCreateTimeCapsule = {},
            )
        }

        // Assumes a capsule called "Future Capsule" is scheduled
        composeTestRule
            .onNodeWithText("Future Capsule")
            .assertIsDisplayed()
    }

    @Test
    fun switchesToSentCapsules_onSentButtonClick() {
        composeTestRule.activity.setContent {
            AllTimeCapsulesScreen(
                onNavigateToViewTimeCapsule = {_, _ -> },
                onNavigateToCreateTimeCapsule = {},
            )
        }

        // Click "Sent" tab
        val sentText = composeTestRule.activity.getString(R.string.sent)
        composeTestRule.onNodeWithText(sentText).performClick()

        // Wait for the capsule to appear
        composeTestRule.waitUntil(
            timeoutMillis = 5_000,
            condition = {
                composeTestRule
                    .onAllNodesWithText("Past Capsule")
                    .fetchSemanticsNodes().isNotEmpty()
            }
        )

        composeTestRule
            .onNodeWithText("Past Capsule")
            .assertIsDisplayed()
    }

    @Test
    fun switchesToReceivedCapsules_onReceivedButtonClick() {
        composeTestRule.activity.setContent {
            AllTimeCapsulesScreen(
                onNavigateToViewTimeCapsule = {_, _ -> },
                onNavigateToCreateTimeCapsule = {},
            )
        }

        val receivedText = composeTestRule.activity.getString(R.string.received)
        composeTestRule.onNodeWithText(receivedText).performClick()

        composeTestRule.waitUntil(
            timeoutMillis = 5_000,
            condition = {
                composeTestRule
                    .onAllNodesWithText("Received Capsule")
                    .fetchSemanticsNodes().isNotEmpty()
            }
        )

        composeTestRule
            .onNodeWithText("Received Capsule")
            .assertIsDisplayed()
    }

    @Test
    fun displaysCreateCapsuleFab_andRespondsToClick() {
        composeTestRule.activity.setContent {
            AllTimeCapsulesScreen(
                onNavigateToViewTimeCapsule = {_, _ -> },
                onNavigateToCreateTimeCapsule = {},
            )
        }

        val fabDesc = composeTestRule.activity.getString(R.string.create_time_capsule)

        composeTestRule
            .onNodeWithContentDescription(fabDesc)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun deleteDialogAppears_whenDeleteOpened() {
        composeTestRule.activity.setContent {
            AllTimeCapsulesScreen(
                onNavigateToViewTimeCapsule = {_, _ -> },
                onNavigateToCreateTimeCapsule = {},
            )
        }

        // Simulate clicking delete on a capsule
        val deleteText = composeTestRule.activity.getString(R.string.delete)
        composeTestRule.onNodeWithContentDescription(deleteText).performClick()

        val dialogTitle = composeTestRule.activity.getString(R.string.confirm_capsule_deletion)
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }
}
