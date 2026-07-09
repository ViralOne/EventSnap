package com.eventsnap.android.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.eventsnap.android.MainActivity
import com.eventsnap.android.R
import com.eventsnap.android.feature.capture.components.CaptureLaunchAction

/**
 * Registers the long-press-the-icon shortcuts as DYNAMIC shortcuts (not static XML) so they resolve
 * to the running app regardless of product flavor — the applicationId differs per flavor (…/.prod)
 * but [Context.getPackageName] is correct at runtime. Each shortcut launches [MainActivity] with the
 * LAUNCH_ACTION extra, which the capture screen reads to auto-start the matching input.
 */
object CaptureShortcuts {
    fun register(context: Context) {
        val shortcuts =
            listOf(
                shortcut(
                    context,
                    id = "describe",
                    action = CaptureLaunchAction.DESCRIBE,
                    shortLabel = context.getString(R.string.shortcut_describe_short),
                    longLabel = context.getString(R.string.shortcut_describe_long),
                    iconRes = R.drawable.ic_shortcut_edit,
                    rank = 0,
                ),
                shortcut(
                    context,
                    id = "photo",
                    action = CaptureLaunchAction.PHOTO,
                    shortLabel = context.getString(R.string.shortcut_photo_short),
                    longLabel = context.getString(R.string.shortcut_photo_long),
                    iconRes = R.drawable.ic_shortcut_camera,
                    rank = 1,
                ),
                shortcut(
                    context,
                    id = "voice",
                    action = CaptureLaunchAction.VOICE,
                    shortLabel = context.getString(R.string.shortcut_voice_short),
                    longLabel = context.getString(R.string.shortcut_voice_long),
                    iconRes = R.drawable.ic_shortcut_mic,
                    rank = 2,
                ),
            )
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    private fun shortcut(
        context: Context,
        id: String,
        action: String,
        shortLabel: String,
        longLabel: String,
        iconRes: Int,
        rank: Int,
    ): ShortcutInfoCompat {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                this.action = Intent.ACTION_VIEW
                putExtra(MainActivity.EXTRA_LAUNCH_ACTION, action)
            }
        return ShortcutInfoCompat
            .Builder(context, id)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setRank(rank)
            .setIntent(intent)
            .build()
    }
}
