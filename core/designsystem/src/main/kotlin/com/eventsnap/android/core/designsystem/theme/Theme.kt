package com.eventsnap.android.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors =
    lightColorScheme(
        primary = EventsnapColors.Primary,
        onPrimary = EventsnapColors.OnPrimary,
        secondary = EventsnapColors.Secondary,
        onSecondary = EventsnapColors.OnSecondary,
        error = EventsnapColors.Error,
        onError = EventsnapColors.OnError,
        background = EventsnapColors.LightBackground,
        surface = EventsnapColors.LightSurface,
        onSurface = EventsnapColors.LightOnSurface,
    )

private val DarkColors =
    darkColorScheme(
        primary = EventsnapColors.Primary,
        onPrimary = EventsnapColors.OnPrimary,
        secondary = EventsnapColors.Secondary,
        onSecondary = EventsnapColors.OnSecondary,
        error = EventsnapColors.Error,
        onError = EventsnapColors.OnError,
        background = EventsnapColors.DarkBackground,
        surface = EventsnapColors.DarkSurface,
        onSurface = EventsnapColors.DarkOnSurface,
    )

@Composable
fun EventsnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColors
            else -> LightColors
        }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EventsnapTypography,
        content = content,
    )
}
