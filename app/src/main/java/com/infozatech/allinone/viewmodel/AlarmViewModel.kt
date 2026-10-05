package com.infozatech.allinone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.infozatech.allinone.alarm.AlarmScheduler
import com.infozatech.allinone.data.Alarm
import com.infozatech.allinone.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).alarmDao()
    private val scheduler = AlarmScheduler(app)

    val alarms: StateFlow<List<Alarm>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Make sure every enabled alarm is armed with the system.
        viewModelScope.launch {
            dao.getEnabled().forEach { scheduler.schedule(it) }
        }
    }

    /** Adds a new alarm (id = 0) or updates an existing one, then (re)schedules it. */
    fun save(alarm: Alarm) {
        viewModelScope.launch {
            val newId = dao.upsert(alarm)
            val saved = if (alarm.id == 0L) alarm.copy(id = newId) else alarm
            scheduler.schedule(saved)
        }
    }

    fun setEnabled(alarm: Alarm, enabled: Boolean) = save(alarm.copy(enabled = enabled))

    fun delete(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm.id)
            dao.delete(alarm)
        }
    }
}
