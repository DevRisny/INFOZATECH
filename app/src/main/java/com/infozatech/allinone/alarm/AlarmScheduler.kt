package com.infozatech.allinone.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.infozatech.allinone.MainActivity
import com.infozatech.allinone.data.Alarm
import java.time.ZonedDateTime

/**
 * Sets and cancels alarms with [AlarmManager].
 *
 * It uses setAlarmClock(), which rings at the exact time even in Doze mode and does not
 * need the special "exact alarm" permission. Android also shows the next alarm in the
 * status bar / lock screen, just like the built-in clock app.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Schedules the next ring for [alarm], or cancels it if the alarm is switched off. */
    fun schedule(alarm: Alarm) {
        cancel(alarm.id)
        if (!alarm.enabled) return
        val trigger = AlarmTime.nextTrigger(alarm.hour, alarm.minute, alarm.repeatDays, ZonedDateTime.now())
        val operation = firePendingIntent(alarm.id, snoozed = false, flags = UPDATE_FLAGS) ?: return
        setAlarmClock(trigger.toInstant().toEpochMilli(), operation)
    }

    /** Rings again after [minutes] without changing the alarm itself. */
    fun snooze(alarm: Alarm, minutes: Int) {
        val operation = firePendingIntent(alarm.id, snoozed = true, flags = UPDATE_FLAGS) ?: return
        setAlarmClock(System.currentTimeMillis() + minutes * 60_000L, operation)
    }

    fun cancel(alarmId: Long) {
        for (snoozed in listOf(false, true)) {
            firePendingIntent(alarmId, snoozed, flags = EXISTING_FLAGS)?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun setAlarmClock(triggerAtMillis: Long, operation: PendingIntent) {
        val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), operation)
    }

    private fun firePendingIntent(alarmId: Long, snoozed: Boolean, flags: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ID, alarmId)
            putExtra(AlarmReceiver.EXTRA_SNOOZED, snoozed)
        }
        val requestCode = alarmId.toInt() + if (snoozed) SNOOZE_CODE_OFFSET else 0
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private companion object {
        const val SNOOZE_CODE_OFFSET = 1_000_000
        const val UPDATE_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        const val EXISTING_FLAGS = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    }
}
