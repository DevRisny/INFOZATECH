package com.infozatech.allinone.alarm

import java.time.Duration
import java.time.ZonedDateTime

/** Pure time helpers for alarms, kept free of Android classes so they can be unit tested. */
object AlarmTime {

    val DAY_SHORT = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val DAY_INITIAL = listOf("M", "T", "W", "T", "F", "S", "S")

    private const val EVERY_DAY = 127          // Mon..Sun
    private const val WEEKDAYS = 31            // Mon..Fri
    private const val WEEKENDS = 96            // Sat + Sun

    /** Bit 0 = Monday ... bit 6 = Sunday. */
    fun dayBit(index: Int): Int = 1 shl index

    /**
     * Works out the next moment this alarm should ring.
     * [repeatDays] is a bit mask (Mon = bit 0 ... Sun = bit 6). 0 means "ring once".
     */
    fun nextTrigger(hour: Int, minute: Int, repeatDays: Int, now: ZonedDateTime): ZonedDateTime {
        val today = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (repeatDays == 0) {
            return if (today.isAfter(now)) today else today.plusDays(1)
        }
        for (offset in 0..7) {
            val candidate = today.plusDays(offset.toLong())
            val bit = 1 shl (candidate.dayOfWeek.value - 1)
            if ((repeatDays and bit) != 0 && candidate.isAfter(now)) return candidate
        }
        return today.plusDays(1)
    }

    fun describeRepeat(repeatDays: Int): String = when (repeatDays) {
        0 -> "Once"
        EVERY_DAY -> "Every day"
        WEEKDAYS -> "Weekdays"
        WEEKENDS -> "Weekends"
        else -> DAY_SHORT.filterIndexed { i, _ -> (repeatDays and dayBit(i)) != 0 }.joinToString(", ")
    }

    /** Text such as "Rings in 7h 20m". */
    fun timeUntil(now: ZonedDateTime, target: ZonedDateTime): String {
        val seconds = Duration.between(now, target).seconds.coerceAtLeast(0)
        val totalMinutes = (seconds + 59) / 60
        if (totalMinutes < 1) return "Rings in less than a minute"
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60
        val parts = buildList {
            if (days > 0) add("${days}d")
            if (hours > 0) add("${hours}h")
            if (minutes > 0) add("${minutes}m")
        }
        return "Rings in " + parts.joinToString(" ")
    }

    /** Returns the clock text and an optional AM/PM suffix (null in 24-hour mode). */
    fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): Pair<String, String?> {
        return if (is24Hour) {
            String.format("%02d:%02d", hour, minute) to null
        } else {
            val h = if (hour % 12 == 0) 12 else hour % 12
            String.format("%d:%02d", h, minute) to (if (hour < 12) "AM" else "PM")
        }
    }
}
