package com.example.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ReminderNotificationsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs get() = context.getSharedPreferences("xasuus_local_reminders", Context.MODE_PRIVATE)

    @Before fun clear() { prefs.edit().clear().commit() }

    @Test fun futureReminderIsStoredAndCanBeCancelled() {
        val time = System.currentTimeMillis() + 60_000
        ReminderNotifications.schedule(context, "note-1", "Test", time)
        assertEquals(time, prefs.getLong("reminder_note-1", 0))
        assertEquals("Test", prefs.getString("title_note-1", null))
        ReminderNotifications.cancel(context, "note-1")
        assertFalse(prefs.contains("reminder_note-1"))
        assertFalse(prefs.contains("title_note-1"))
    }

    @Test fun pastReminderRemovesExistingReminder() {
        ReminderNotifications.schedule(context, "note-1", "Test", System.currentTimeMillis() + 60_000)
        ReminderNotifications.schedule(context, "note-1", "Test", System.currentTimeMillis() - 1)
        assertFalse(prefs.contains("reminder_note-1"))
    }
}
