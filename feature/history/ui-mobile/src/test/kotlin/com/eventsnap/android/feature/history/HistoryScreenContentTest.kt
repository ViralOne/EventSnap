package com.eventsnap.android.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventsnap.android.feature.history.components.HistoryScreenContent
import com.eventsnap.android.feature.history.data.HistoryItem
import com.eventsnap.android.feature.history.mvi.HistoryState
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `empty state is shown when there are no items`() {
        composeTestRule.setContent {
            HistoryScreenContent(state = HistoryState(isLoading = false), onOpenEvent = {}, onRestoreEvent = {})
        }
        composeTestRule.onNodeWithTag("history_empty").assertIsDisplayed()
    }

    @Test
    fun `tapping a row opens its calendar event`() {
        val opened = mutableListOf<Long>()
        composeTestRule.setContent {
            HistoryScreenContent(
                state =
                    HistoryState(
                        isLoading = false,
                        items =
                            persistentListOf(
                                HistoryItem(42, "Dinner", 1_000L, false, null, calendarEventId = 777, createdAtEpochMillis = 1_000L),
                            ),
                    ),
                onOpenEvent = { opened.add(it) },
                onRestoreEvent = {},
            )
        }
        composeTestRule.onNodeWithTag("history_row_42").performClick()
        assertThat(opened).containsExactly(777L)
    }

    @Test
    fun `a deleted event is labelled and does not open`() {
        val opened = mutableListOf<Long>()
        composeTestRule.setContent {
            HistoryScreenContent(
                state =
                    HistoryState(
                        isLoading = false,
                        items = persistentListOf(deletedItem()),
                    ),
                onOpenEvent = { opened.add(it) },
                onRestoreEvent = {},
            )
        }
        composeTestRule.onNodeWithTag("history_deleted_42").assertIsDisplayed()
        composeTestRule.onNodeWithTag("history_row_42").performClick()
        assertThat(opened).isEmpty()
    }

    @Test
    fun `confirming the restore dialog restores the event`() {
        val restored = mutableListOf<Long>()
        composeTestRule.setContent {
            HistoryScreenContent(
                state =
                    HistoryState(
                        isLoading = false,
                        items = persistentListOf(deletedItem()),
                    ),
                onOpenEvent = {},
                onRestoreEvent = { restored.add(it) },
            )
        }
        composeTestRule.onNodeWithTag("history_restore_42").performClick()
        composeTestRule.onNodeWithTag("history_restore_confirm_42").performClick()
        assertThat(restored).containsExactly(42L)
    }

    private fun deletedItem() =
        HistoryItem(
            id = 42,
            title = "Dinner",
            startEpochMillis = 1_000L,
            allDay = false,
            location = null,
            calendarEventId = 777,
            createdAtEpochMillis = 1_000L,
            deletedFromCalendar = true,
        )
}
