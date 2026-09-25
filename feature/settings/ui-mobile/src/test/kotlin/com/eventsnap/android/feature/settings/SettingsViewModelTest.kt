package com.eventsnap.android.feature.settings

import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm
import com.eventsnap.android.core.model.ThemePreference
import com.eventsnap.android.feature.settings.data.SettingsRepository
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.testing.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
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
            whenever(themePreference).thenReturn(flowOf(ThemePreference.SYSTEM))
            whenever(dynamicColor).thenReturn(flowOf(true))
            whenever(textModel).thenReturn(flowOf(null))
            whenever(visionModel).thenReturn(flowOf(null))
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
    fun `selecting a theme persists it and reflects in state`() =
        runTest {
            val repo = repository()
            whenever(repo.writableCalendars()).thenReturn(emptyList())

            val vm = SettingsViewModel(repo)
            vm.setAction(SettingsAction.ThemeSelected(ThemePreference.DARK))

            verify(repo).setThemePreference(ThemePreference.DARK)
            assertThat(vm.state.value.themePreference).isEqualTo(ThemePreference.DARK)
        }

    @Test
    fun `live models are loaded into state and can be pinned per arm`() =
        runTest {
            val repo = repository()
            whenever(repo.writableCalendars()).thenReturn(emptyList())
            whenever(repo.availableModels(any())).thenReturn(listOf(VISION_MODEL))
            whenever(repo.resolvedModel(any())).thenReturn(VISION_MODEL.id)

            val vm = SettingsViewModel(repo)
            assertThat(vm.state.value.availableModels).containsExactly(VISION_MODEL)
            assertThat(vm.state.value.resolvedVisionModel).isEqualTo(VISION_MODEL.id)

            vm.setAction(SettingsAction.ModelSelected(AiModelArm.VISION, VISION_MODEL.id))

            verify(repo).setModel(AiModelArm.VISION, VISION_MODEL.id)
            assertThat(vm.state.value.pinnedVisionModel).isEqualTo(VISION_MODEL.id)
        }

    @Test
    fun `choosing automatic clears the pinned model`() =
        runTest {
            val repo = repository()
            whenever(repo.writableCalendars()).thenReturn(emptyList())
            whenever(repo.availableModels(any())).thenReturn(listOf(VISION_MODEL))

            val vm = SettingsViewModel(repo)
            vm.setAction(SettingsAction.ModelSelected(AiModelArm.TEXT, null))

            verify(repo).setModel(AiModelArm.TEXT, null)
            assertThat(vm.state.value.pinnedTextModel).isNull()
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

    private companion object {
        val VISION_MODEL = AiModel(id = "qwen/qwen3.8-27b", ownedBy = "Alibaba Cloud", supportsVision = true)
    }
}
