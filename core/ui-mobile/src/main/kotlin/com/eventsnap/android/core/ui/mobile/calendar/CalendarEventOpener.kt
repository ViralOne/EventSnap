package com.eventsnap.android.core.ui.mobile.calendar

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

/**
 * Opens a calendar event that EventSnap created, in whatever calendar app the user has
 * (Google Calendar, etc.), using standard CalendarContract intents. No extra permissions.
 */
object CalendarEventOpener {
    /** True if [eventId] refers to a real inserted event (rows created before id-tracking are -1). */
    fun canOpen(eventId: Long): Boolean = eventId > 0

    /** Opens the event in view mode. Returns false if no calendar app handled the intent. */
    fun view(
        context: Context,
        eventId: Long,
    ): Boolean = launch(context, Intent.ACTION_VIEW, eventId)

    /** Opens the event in the calendar app's editor. Returns false if nothing handled it. */
    fun edit(
        context: Context,
        eventId: Long,
    ): Boolean = launch(context, Intent.ACTION_EDIT, eventId)

    private fun launch(
        context: Context,
        action: String,
        eventId: Long,
    ): Boolean {
        if (!canOpen(eventId)) return false
        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val intent = Intent(action).setData(uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
