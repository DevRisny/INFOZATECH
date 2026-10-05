package com.infozatech.allinone.ui.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infozatech.allinone.data.Priority
import com.infozatech.allinone.data.Todo
import com.infozatech.allinone.ui.components.EmptyState
import com.infozatech.allinone.ui.components.ScreenHeader
import com.infozatech.allinone.viewmodel.TodoFilter
import com.infozatech.allinone.viewmodel.TodoViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LowColor = Color(0xFF2E9E5B)
private val MediumColor = Color(0xFFF2A100)
private val HighColor = Color(0xFFE5484D)

private fun priorityColor(priority: Int): Color = when (priority) {
    Priority.LOW -> LowColor
    Priority.HIGH -> HighColor
    else -> MediumColor
}

private fun priorityName(priority: Int): String = when (priority) {
    Priority.LOW -> "Low"
    Priority.HIGH -> "High"
    else -> "Medium"
}

private fun dueLabel(dueDay: Long): String {
    val today = LocalDate.now().toEpochDay()
    val date = LocalDate.ofEpochDay(dueDay)
    val pretty = date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    return when {
        dueDay == today -> "Today"
        dueDay == today + 1 -> "Tomorrow"
        dueDay < today -> "Overdue · $pretty"
        else -> pretty
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(vm: TodoViewModel = viewModel()) {
    val all by vm.allTodos.collectAsStateWithLifecycle()
    val visible by vm.visibleTodos.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Todo?>(null) }

    val doneCount = all.count { it.done }
    val progress = if (all.isEmpty()) 0f else doneCount.toFloat() / all.size

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editing = null
                    showDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New task") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(
                title = "Tasks",
                subtitle = if (all.isEmpty()) "Nothing to do yet" else "$doneCount of ${all.size} done",
                actions = {
                    if (doneCount > 0) {
                        IconButton(onClick = {
                            vm.clearCompleted()
                            scope.launch { snackbar.showSnackbar("Completed tasks cleared") }
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear completed")
                        }
                    }
                },
            )

            if (all.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TodoFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { vm.setFilter(option) },
                            label = { Text(option.label) },
                        )
                    }
                }
            }

            if (visible.isEmpty()) {
                if (all.isEmpty()) {
                    EmptyState(
                        emoji = "✅",
                        title = "No tasks yet",
                        message = "Tap “New task” to add your first one.",
                    )
                } else {
                    EmptyState(
                        emoji = "🔍",
                        title = "Nothing here",
                        message = "No ${filter.label.lowercase()} tasks right now.",
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(visible, key = { it.id }) { todo ->
                        TodoRow(
                            todo = todo,
                            onToggle = { vm.toggle(todo) },
                            onEdit = {
                                editing = todo
                                showDialog = true
                            },
                            onDelete = {
                                vm.delete(todo)
                                scope.launch {
                                    snackbar.currentSnackbarData?.dismiss()
                                    val result = snackbar.showSnackbar(
                                        message = "Task deleted",
                                        actionLabel = "Undo",
                                    )
                                    if (result == SnackbarResult.ActionPerformed) vm.save(todo)
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        TodoDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { title, priority, dueDay ->
                val base = editing ?: Todo(title = title)
                vm.save(base.copy(title = title, priority = priority, dueDay = dueDay))
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoRow(
    todo: Todo,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val swiping = dismissState.dismissDirection != SwipeToDismissBoxValue.Settled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (swiping) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                },
            ) {
                if (swiping) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        },
    ) {
        Card(
            onClick = onEdit,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = todo.done, onCheckedChange = { onToggle() })
                Column(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                        color = if (todo.done) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(priorityColor(todo.priority)),
                        )
                        Text(
                            text = "  ${priorityName(todo.priority)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        todo.dueDay?.let { day ->
                            val overdue = !todo.done && day < LocalDate.now().toEpochDay()
                            Text(
                                text = "   ·   ${dueLabel(day)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (overdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoDialog(
    initial: Todo?,
    onDismiss: () -> Unit,
    onSave: (title: String, priority: Int, dueDay: Long?) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var priority by remember { mutableStateOf(initial?.priority ?: Priority.MEDIUM) }
    var dueDay by remember { mutableStateOf(initial?.dueDay) }
    var showPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New task" else "Edit task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs to be done?") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH).forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(priorityName(option)) },
                        )
                    }
                }

                Text("Due date", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showPicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dueDay?.let { dueLabel(it) } ?: "Set date")
                    }
                    if (dueDay != null) {
                        IconButton(onClick = { dueDay = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove due date")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.trim(), priority, dueDay) },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDay?.let { it * MILLIS_PER_DAY },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dueDay = Math.floorDiv(it, MILLIS_PER_DAY) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
