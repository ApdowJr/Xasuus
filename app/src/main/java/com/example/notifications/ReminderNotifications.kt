package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/** Local, OS-scheduled reminders. AlarmManager keeps these alive when the app process is gone. */
object ReminderNotifications {
    const val CHANNEL_ID = "xasuus_note_reminders"
    private const val PREFS = "xasuus_local_reminders"
    private const val KEY_PREFIX = "reminder_"
    private const val TITLE_PREFIX = "title_"
    const val ACTION_FIRE_REMINDER = "com.example.action.FIRE_NOTE_REMINDER"

    fun schedule(context: Context, noteId: String, title: String, atMillis: Long?) {
        if (noteId.isBlank()) return
        if (atMillis == null || atMillis <= System.currentTimeMillis()) {
            cancel(context, noteId)
            return
        }
        createChannel(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_PREFIX + noteId, atMillis)
            .putString(TITLE_PREFIX + noteId, title)
            .apply()
        setAlarm(context, noteId, title, atMillis)
    }

    fun cancel(context: Context, noteId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, noteId, ""))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PREFIX + noteId)
            .remove(TITLE_PREFIX + noteId)
            .apply()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Xasuus reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ogeysiisyada xasuusaha aad waqtiga u qorshaysay"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    internal fun restoreAfterReboot(context: Context) {
        createChannel(context)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith(KEY_PREFIX) && value is Long && value > now) {
                val noteId = key.removePrefix(KEY_PREFIX)
                val title = prefs.getString(TITLE_PREFIX + noteId, "Xasuus") ?: "Xasuus"
                setAlarm(context, noteId, title, value)
            }
        }
    }

    private fun setAlarm(context: Context, noteId: String, title: String, atMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = pendingIntent(context, noteId, title)
        // Inexact idle-friendly alarm: avoids the special exact-alarm access permission while
        // still reliably surfacing the reminder when the app is closed or the phone is idle.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, alarmIntent)
    }

    private fun pendingIntent(context: Context, noteId: String, title: String): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_FIRE_REMINDER
            putExtra("note_id", noteId)
            putExtra("note_title", title)
        }
        return PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderNotifications.restoreAfterReboot(context)
            return
        }
        if (intent.action != ReminderNotifications.ACTION_FIRE_REMINDER) return

        val noteId = intent.getStringExtra("note_id").orEmpty()
        val title = intent.getStringExtra("note_title")?.takeIf { it.isNotBlank() } ?: "Xasuus la qorsheeyay"
        ReminderNotifications.cancel(context, noteId)
        ReminderNotifications.createChannel(context)

        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val openApp = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_note_id", noteId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderNotifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Xasuus-ku-xasuusin")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Waa waqtigii xasuustaada: $title"))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(noteId.hashCode(), notification)
    }
}
