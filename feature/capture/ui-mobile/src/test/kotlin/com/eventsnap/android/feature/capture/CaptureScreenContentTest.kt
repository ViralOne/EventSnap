package com.eventsnap.android.feature.capture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eventsnap.android.feature.capture.components.CaptureLaunchers
import com.eventsnap.android.feature.capture.components.CaptureScreenContent
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureState
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptureScreenContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val capturedActions = mutableListOf<CaptureAction>()
    private var galleryClicks = 0
    private var voiceClicks = 0

    private fun setContent(state: CaptureState = CaptureState()) {
        composeTestRule.setContent {
            CaptureScreenContent(
                state = state,
                onAction = { capturedActions.add(it) },
                launchers =
                    CaptureLaunchers(
                        onPickFromGallery = { galleryClicks++ },
                        onTakePhoto = {},
                        onPickFromFiles = {},
                        onStartVoice = { voiceClicks++ },
                    ),
            )
        }
    }

    @Test
    fun `typing dispatches DescriptionChanged`() {
        setContent()
        composeTestRule.onNodeWithTag("capture_description_field").performTextInput("Lunch")
        assertThat(capturedActions.any { it is CaptureAction.DescriptionChanged }).isTrue()
    }

    @Test
    fun `send button dispatches SubmitText when there is text`() {
        setContent(state = CaptureState(description = "Lunch noon"))
        composeTestRule.onNodeWithTag("capture_send").performClick()
        assertThat(capturedActions).contains(CaptureAction.SubmitText)
    }

    @Test
    fun `mic button triggers voice when field is empty`() {
        setContent()
        composeTestRule.onNodeWithTag("capture_voice").performClick()
        assertThat(voiceClicks).isEqualTo(1)
    }

    @Test
    fun `gallery row triggers gallery picker`() {
        setContent()
        composeTestRule.onNodeWithTag("action_Select from gallery").performScrollTo().performClick()
        assertThat(galleryClicks).isEqualTo(1)
    }

    @Test
    fun `error text is shown when state has an error`() {
        setContent(state = CaptureState(error = "boom"))
        composeTestRule.onNodeWithTag("capture_error").performScrollTo().assertIsDisplayed()
    }
}
