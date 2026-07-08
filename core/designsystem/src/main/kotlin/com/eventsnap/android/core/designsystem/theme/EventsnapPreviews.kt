package com.eventsnap.android.core.designsystem.theme

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation: renders a composable in both light and dark. Use this alone —
 * never stack a bare @Preview on top of it.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class EventsnapPreviews
