package com.eventsnap.android.feature.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventsnap.android.feature.history.components.HistoryScreenContent
import com.eventsnap.android.feature.history.data.HistoryItem
import com.eventsnap.android.feature.history.mvi.HistoryState
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
            HistoryScreenContent(state = HistoryState(isLoading = false))
        }
        composeTestRule.onNodeWithTag("history_empty").assertIsDisplayed()
    }

    @Test
    fun `history rows are rendered for items`() {
        composeTestRule.setContent {
            HistoryScreenContent(
                state =
                    HistoryState(
                        isLoading = false,
                        items =
                            persistentListOf(
                                HistoryItem(42, "Dinner", 1_000L, false, null, 1_000L),
                            ),
                    ),
            )
        }
        composeTestRule.onNodeWithTag("history_row_42").assertIsDisplayed()
    }
}
