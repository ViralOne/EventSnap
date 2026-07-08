package com.eventsnap.android.feature.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.feature.review.components.ReviewScreenContent
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewState
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReviewScreenContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val capturedActions = mutableListOf<ReviewAction>()

    private val sampleState =
        ReviewState(
            events =
                persistentListOf(
                    CalendarEvent(title = "Dinner", startEpochMillis = 1_000L, endEpochMillis = 2_000L),
                ),
            calendars =
                persistentListOf(
                    TargetCalendar(id = 1, displayName = "Personal", accountName = "me", isPrimary = true),
                ),
            selectedCalendarId = 1,
        )

    private fun setContent(state: ReviewState = sampleState) {
        composeTestRule.setContent {
            ReviewScreenContent(state = state, onAction = { capturedActions.add(it) })
        }
    }

    @Test
    fun `tapping add dispatches Confirm`() {
        setContent()
        composeTestRule.onNodeWithTag("review_confirm").performClick()
        assertThat(capturedActions).contains(ReviewAction.Confirm)
    }

    @Test
    fun `event card is rendered for each event`() {
        setContent()
        composeTestRule.onNodeWithTag("event_card_0").assertIsDisplayed()
    }
}
