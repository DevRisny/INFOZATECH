package com.infozatech.allinone.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.infozatech.allinone.MainActivity
import com.infozatech.allinone.R
import com.infozatech.allinone.data.Alarm

object AlarmNotifications {

    private const val CHANNEL_ID = "alarms"
    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Rings when one of your alarms goes off"
            setSound(sound, attributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    /** Shows the ringing notification with Snooze and Dismiss buttons. */
    @SuppressLint("MissingPermission") // We only post if notifications are enabled.
    fun show(context: Context, alarm: Alarm) {
        ensureChannel(context)
        val notificationId = alarm.id.toInt()

        val openApp = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            FLAGS,
        )
        val snooze = PendingIntent.getBroadcast(
            context,
            notificationId + 2_000_000,
            actionIntent(context, AlarmReceiver.ACTION_SNOOZE, alarm.id),
            FLAGS,
        )
        val dismiss = PendingIntent.getBroadcast(
            context,
            notificationId + 3_000_000,
            actionIntent(context, AlarmReceiver.ACTION_DISMISS, alarm.id),
            FLAGS,
        )

        val (clock, suffix) = AlarmTime.formatTime(alarm.hour, alarm.minute, DateFormat.is24HourFormat(context))
        val timeText = if (suffix == null) clock else "$clock $suffix"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_alarm)
            .setContentTitle(alarm.label.ifBlank { "Alarm" })
            .setContentText(timeText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(0, "Snooze 10 min", snooze)
            .addAction(0, "Dismiss", dismiss)
            .build()

        // Keep the sound repeating until the user snoozes, dismisses or swipes it away.
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        val manager = NotificationManagerCompat.from(context)
        if (manager.areNotificationsEnabled()) {
            manager.notify(notificationId, notification)
        }
    }

    fun cancel(context: Context, alarmId: Long) {
        NotificationManagerCompat.from(context).cancel(alarmId.toInt())
    }

    private fun actionIntent(context: Context, action: String, alarmId: Long) =
        Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmReceiver.EXTRA_ID, alarmId)
        }
}
