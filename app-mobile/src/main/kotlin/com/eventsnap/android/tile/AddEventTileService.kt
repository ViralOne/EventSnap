package com.eventsnap.android.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.eventsnap.android.MainActivity
import com.eventsnap.android.feature.capture.components.CaptureLaunchAction

/**
 * A Quick Settings tile that jumps straight into EventSnap's capture screen, ready to describe an
 * event. Uses the modern activity-launch API on Android 14+ and falls back to a legacy launch below.
 */
class AddEventTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent =
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_LAUNCH_ACTION, CaptureLaunchAction.DESCRIBE)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent(intent))
        } else {
            legacyStart(intent)
        }
    }

    // The Intent overload is deprecated in API 34+, but it's the only option on our minSdk (29)
    // through 33; the PendingIntent overload above is used on 34+. Suppressed intentionally.
    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun legacyStart(intent: Intent) {
        startActivityAndCollapse(intent)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun pendingIntent(intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
