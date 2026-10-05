package com.infozatech.allinone.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.infozatech.allinone.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Android forgets alarms when the phone restarts, so this schedules every enabled alarm again. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scheduler = AlarmScheduler(appContext)
                AppDatabase.get(appContext).alarmDao().getEnabled().forEach { scheduler.schedule(it) }
            } finally {
                pending.finish()
            }
        }
    }
}
