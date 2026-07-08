package com.eventsnap.android.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventsnap.android.feature.settings.components.SettingsScreenContent
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.feature.settings.mvi.SettingsState
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val capturedActions = mutableListOf<SettingsAction>()

    private fun setContent(state: SettingsState = SettingsState()) {
        composeTestRule.setContent {
            SettingsScreenContent(state = state, onAction = { capturedActions.add(it) })
        }
    }

    @Test
    fun `typing api key dispatches ApiKeyChanged`() {
        setContent()
        composeTestRule.onNodeWithTag("settings_api_key_field").performTextInput("gsk")
        assertThat(capturedActions.any { it is SettingsAction.ApiKeyChanged }).isTrue()
    }

    @Test
    fun `tapping save dispatches SaveApiKey`() {
        setContent()
        composeTestRule.onNodeWithTag("settings_save_key").performClick()
        assertThat(capturedActions).contains(SettingsAction.SaveApiKey)
    }
}
