package com.eventsnap.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.eventsnap.android.MainActivity
import com.eventsnap.android.R
import com.eventsnap.android.feature.capture.components.CaptureLaunchAction

/**
 * Home-screen widget: a quick-add bar that opens EventSnap ready to describe an event, plus a mic
 * button that opens straight into voice input. Both launch [MainActivity] with a LAUNCH_ACTION
 * extra that the capture screen reads.
 */
class AddEventWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id ->
            val views =
                RemoteViews(context.packageName, R.layout.widget_add_event).apply {
                    setOnClickPendingIntent(R.id.widget_add_bar, launchIntent(context, CaptureLaunchAction.DESCRIBE, 0))
                    setOnClickPendingIntent(R.id.widget_mic, launchIntent(context, CaptureLaunchAction.VOICE, 1))
                }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun launchIntent(
        context: Context,
        action: String,
        requestCode: Int,
    ): PendingIntent {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                this.action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_LAUNCH_ACTION, action)
            }
        // Distinct request codes so the two buttons get distinct PendingIntents (same extras otherwise collide).
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
