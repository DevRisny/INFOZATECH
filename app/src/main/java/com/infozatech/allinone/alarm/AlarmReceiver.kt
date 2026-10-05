package com.infozatech.allinone.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.infozatech.allinone.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Receives the alarm broadcast and the Snooze / Dismiss notification buttons. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handle(appContext, intent)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ID, -1L)
        if (alarmId < 0) return

        val dao = AppDatabase.get(context).alarmDao()
        val scheduler = AlarmScheduler(context)

        when (intent.action) {
            ACTION_FIRE -> {
                val alarm = dao.getById(alarmId) ?: return
                AlarmNotifications.show(context, alarm)

                // A snooze ring leaves the alarm as it is. A normal ring either schedules
                // the next repeat, or switches a one-time alarm off.
                if (!intent.getBooleanExtra(EXTRA_SNOOZED, false)) {
                    if (alarm.repeatDays != 0) {
                        scheduler.schedule(alarm)
                    } else {
                        dao.upsert(alarm.copy(enabled = false))
                    }
                }
            }
            ACTION_SNOOZE -> {
                AlarmNotifications.cancel(context, alarmId)
                dao.getById(alarmId)?.let { scheduler.snooze(it, SNOOZE_MINUTES) }
            }
            ACTION_DISMISS -> AlarmNotifications.cancel(context, alarmId)
        }
    }

    companion object {
        const val ACTION_FIRE = "com.infozatech.allinone.ALARM_FIRE"
        const val ACTION_SNOOZE = "com.infozatech.allinone.ALARM_SNOOZE"
        const val ACTION_DISMISS = "com.infozatech.allinone.ALARM_DISMISS"
        const val EXTRA_ID = "alarm_id"
        const val EXTRA_SNOOZED = "snoozed"
        const val SNOOZE_MINUTES = 10
    }
}
