package com.infozatech.allinone

import com.infozatech.allinone.alarm.AlarmTime
import org.junit.Assert.assertEquals
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Test

class AlarmTimeTest {

    private val zone = ZoneId.of("Asia/Colombo")

    // Wednesday 7 October 2026, 10:00
    private val now = ZonedDateTime.of(2026, 10, 7, 10, 0, 0, 0, zone)

    @Test fun onceLaterTodayRingsToday() {
        val t = AlarmTime.nextTrigger(18, 30, 0, now)
        assertEquals(7, t.dayOfMonth)
        assertEquals(18, t.hour)
    }

    @Test fun onceEarlierTodayRingsTomorrow() {
        val t = AlarmTime.nextTrigger(6, 0, 0, now)
        assertEquals(8, t.dayOfMonth)
    }

    @Test fun sameMinuteAsNowRingsTomorrow() {
        val t = AlarmTime.nextTrigger(10, 0, 0, now)
        assertEquals(8, t.dayOfMonth)
    }

    @Test fun repeatPicksNextMatchingDay() {
        // Mondays only (bit 0): next Monday after Wed 7 Oct is 12 Oct.
        val t = AlarmTime.nextTrigger(9, 0, AlarmTime.dayBit(0), now)
        assertEquals(12, t.dayOfMonth)
    }

    @Test fun repeatTodayIfStillAhead() {
        // Wednesdays only (bit 2) at 20:00 rings today.
        val t = AlarmTime.nextTrigger(20, 0, AlarmTime.dayBit(2), now)
        assertEquals(7, t.dayOfMonth)
    }

    @Test fun repeatTodayButPassedGoesToNextWeek() {
        val t = AlarmTime.nextTrigger(8, 0, AlarmTime.dayBit(2), now)
        assertEquals(14, t.dayOfMonth)
    }

    @Test fun describeRepeatLabels() {
        assertEquals("Once", AlarmTime.describeRepeat(0))
        assertEquals("Every day", AlarmTime.describeRepeat(127))
        assertEquals("Weekdays", AlarmTime.describeRepeat(31))
        assertEquals("Weekends", AlarmTime.describeRepeat(96))
        assertEquals("Mon, Wed", AlarmTime.describeRepeat(AlarmTime.dayBit(0) or AlarmTime.dayBit(2)))
    }

    @Test fun timeUntilFormatting() {
        assertEquals("Rings in 8h", AlarmTime.timeUntil(now, now.plusHours(8)))
        assertEquals("Rings in 1d 2h 5m", AlarmTime.timeUntil(now, now.plusDays(1).plusHours(2).plusMinutes(5)))
        assertEquals("Rings in less than a minute", AlarmTime.timeUntil(now, now.plusSeconds(20).minusSeconds(20)))
    }

    @Test fun formatTime12And24() {
        assertEquals("7:05" to "AM", AlarmTime.formatTime(7, 5, false))
        assertEquals("12:00" to "PM", AlarmTime.formatTime(12, 0, false))
        assertEquals("12:30" to "AM", AlarmTime.formatTime(0, 30, false))
        assertEquals("18:45" to null, AlarmTime.formatTime(18, 45, true))
    }
}
