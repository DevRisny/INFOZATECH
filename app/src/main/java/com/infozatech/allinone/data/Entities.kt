package com.infozatech.allinone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A note shown in the Notes tab. [colorIndex] picks one of the card colours. */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val colorIndex: Int = 0,
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

object Priority {
    const val LOW = 0
    const val MEDIUM = 1
    const val HIGH = 2
}

/** A to-do item. [dueDay] is a LocalDate epoch day, or null when there is no due date. */
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val done: Boolean = false,
    val priority: Int = Priority.MEDIUM,
    val dueDay: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** An alarm. [repeatDays] is a bit mask: Monday = bit 0 ... Sunday = bit 6. 0 means ring once. */
@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val enabled: Boolean = true,
    val repeatDays: Int = 0,
)
