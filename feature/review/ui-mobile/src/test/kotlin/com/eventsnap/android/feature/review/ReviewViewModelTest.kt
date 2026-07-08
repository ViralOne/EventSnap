package com.eventsnap.android.feature.review

import app.cash.turbine.test
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.feature.review.data.ReviewRepository
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewEffect
import com.eventsnap.android.testing.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ReviewViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val event = CalendarEvent(title = "Dinner", startEpochMillis = 1_000L, endEpochMillis = 2_000L)

    @Test
    fun `confirm writes events and emits ShowSaved then NavigateBack`() =
        runTest {
            val repo = mock<ReviewRepository>()
            whenever(repo.pendingEvents()).thenReturn(listOf(event))
            whenever(repo.writableCalendars()).thenReturn(
                listOf(TargetCalendar(id = 7, displayName = "Personal", accountName = "me", isPrimary = true)),
            )
            whenever(repo.defaultCalendarId()).thenReturn(7)

            val vm = ReviewViewModel(repo)
            vm.effects.test {
                vm.setAction(ReviewAction.Confirm)
                assertThat(awaitItem()).isEqualTo(ReviewEffect.ShowSaved(1))
                assertThat(awaitItem()).isEqualTo(ReviewEffect.NavigateBackToCapture)
            }
        }

    @Test
    fun `pending events are loaded into state`() =
        runTest {
            val repo = mock<ReviewRepository>()
            whenever(repo.pendingEvents()).thenReturn(listOf(event))
            whenever(repo.writableCalendars()).thenReturn(emptyList())
            whenever(repo.defaultCalendarId()).thenReturn(null)

            val vm = ReviewViewModel(repo)
            vm.state.test {
                assertThat(awaitItem().events).containsExactly(event)
            }
        }
}
