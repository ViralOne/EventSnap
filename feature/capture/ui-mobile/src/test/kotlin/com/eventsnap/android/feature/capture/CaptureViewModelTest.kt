package com.eventsnap.android.feature.capture

import app.cash.turbine.test
import com.eventsnap.android.core.data.handoff.ExtractedEventsHolder
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.CaptureInput
import com.eventsnap.android.feature.capture.data.CaptureRepository
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureEffect
import com.eventsnap.android.testing.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CaptureViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val repository = mock<CaptureRepository> { whenever(it.hasApiKey).thenReturn(flowOf(true)) }
    private val holder = ExtractedEventsHolder()

    private fun viewModel() = CaptureViewModel(repository, holder)

    @Test
    fun `successful extraction emits NavigateToReview and stores events`() =
        runTest {
            val events =
                listOf(
                    CalendarEvent(title = "Dinner", startEpochMillis = 1_000L, endEpochMillis = 2_000L),
                )
            whenever(repository.extractEvents(CaptureInput.Text("Dinner"))).thenReturn(events)

            val vm = viewModel()
            vm.effects.test {
                vm.setAction(CaptureAction.DescriptionChanged("Dinner"))
                vm.setAction(CaptureAction.SubmitText)
                assertThat(awaitItem()).isEqualTo(CaptureEffect.NavigateToReview)
            }
            assertThat(holder.events.value).isEqualTo(events)
        }

    @Test
    fun `blank description sets an error and does not call the repository`() =
        runTest {
            val vm = viewModel()
            vm.setAction(CaptureAction.SubmitText)
            vm.state.test {
                assertThat(awaitItem().error).isNotNull()
            }
        }

    @Test
    fun `OpenApiKeySetup emits NavigateToSettings`() =
        runTest {
            val vm = viewModel()
            vm.effects.test {
                vm.setAction(CaptureAction.OpenApiKeySetup)
                assertThat(awaitItem()).isEqualTo(CaptureEffect.NavigateToSettings)
            }
        }

    @Test
    fun `MediaError surfaces the message and stops processing`() =
        runTest {
            val vm = viewModel()
            vm.setAction(CaptureAction.MediaError("That file looks empty or unreadable."))
            vm.state.test {
                val state = awaitItem()
                assertThat(state.error).isEqualTo("That file looks empty or unreadable.")
                assertThat(state.isProcessing).isFalse()
            }
        }
}
