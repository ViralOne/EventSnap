package com.eventsnap.android.feature.settings

import com.eventsnap.android.feature.settings.data.SettingsRepository
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.testing.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SettingsViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private fun repository() =
        mock<SettingsRepository>().apply {
            whenever(groqApiKey).thenReturn(flowOf(null))
            whenever(defaultCalendarId).thenReturn(flowOf(null))
            whenever(defaultReminderMinutes).thenReturn(flowOf(30))
        }

    @Test
    fun `saving a key persists it and reflects in state`() =
        runTest {
            val repo = repository()
            whenever(repo.writableCalendars()).thenReturn(emptyList())

            val vm = SettingsViewModel(repo)
            vm.setAction(SettingsAction.ApiKeyChanged("gsk_test"))
            vm.setAction(SettingsAction.SaveApiKey)

            verify(repo).setGroqApiKey("gsk_test")
            assertThat(vm.state.value.hasSavedKey).isTrue()
        }

    @Test
    fun `blank key is not persisted`() =
        runTest {
            val repo = repository()
            whenever(repo.writableCalendars()).thenReturn(emptyList())

            val vm = SettingsViewModel(repo)
            vm.setAction(SettingsAction.SaveApiKey)

            assertThat(vm.state.value.savedMessage).isNotNull()
        }
}
