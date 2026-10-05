package com.infozatech.allinone.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infozatech.allinone.data.Note
import com.infozatech.allinone.ui.components.EmptyState
import com.infozatech.allinone.ui.components.ScreenHeader
import com.infozatech.allinone.viewmodel.NotesViewModel
import java.text.DateFormat
import java.util.Date

// Card colours. Index 0 means "use the default surface colour".
private val LightNoteColors = listOf(
    Color.Unspecified,
    Color(0xFFFFF1B8), // yellow
    Color(0xFFFFD6D6), // pink
    Color(0xFFD3F5E1), // green
    Color(0xFFD6E8FF), // blue
    Color(0xFFE6D9FF), // purple
)
private val DarkNoteColors = listOf(
    Color.Unspecified,
    Color(0xFF5C5320),
    Color(0xFF5E3434),
    Color(0xFF235239),
    Color(0xFF23415E),
    Color(0xFF41306B),
)

@Composable
private fun noteBackground(index: Int): Color {
    val palette = if (isSystemInDarkTheme()) DarkNoteColors else LightNoteColors
    val color = palette.getOrElse(index) { Color.Unspecified }
    return if (color == Color.Unspecified) MaterialTheme.colorScheme.surface else color
}

@Composable
fun NotesScreen(vm: NotesViewModel = viewModel()) {
    val editingId by vm.editingId.collectAsStateWithLifecycle()
    val allNotes by vm.allNotes.collectAsStateWithLifecycle()

    val id = editingId
    if (id != null) {
        val existing = if (id == NotesViewModel.NEW_NOTE) null else allNotes.firstOrNull { it.id == id }
        if (id != NotesViewModel.NEW_NOTE && existing == null) {
            // The note no longer exists (for example it was deleted), so go back to the list.
            LaunchedEffect(id) { vm.closeEditor() }
        } else {
            NoteEditor(
                initial = existing,
                onSave = { vm.save(it) },
                onDelete = if (existing != null) ({ vm.delete(existing) }) else null,
                onClose = { vm.closeEditor() },
            )
        }
    } else {
        NotesList(vm)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun NotesList(vm: NotesViewModel) {
    val notes by vm.visibleNotes.collectAsStateWithLifecycle()
    val allNotes by vm.allNotes.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.openEditor(NotesViewModel.NEW_NOTE) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New note") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(
                title = "Notes",
                subtitle = when (allNotes.size) {
                    0 -> "Capture your ideas"
                    1 -> "1 note"
                    else -> "${allNotes.size} notes"
                },
            )

            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Search notes by title") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            if (notes.isEmpty()) {
                if (allNotes.isEmpty()) {
                    EmptyState(
                        emoji = "📝",
                        title = "No notes yet",
                        message = "Tap “New note” to write your first one.",
                    )
                } else {
                    EmptyState(
                        emoji = "🔍",
                        title = "No matches",
                        message = "No note matches “${query.trim()}”.",
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { vm.openEditor(note.id) },
                            onLongClick = { deleteTarget = note },
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { note ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete note?") },
            text = { Text("“${note.title.ifBlank { "Untitled" }}” will be removed for good.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(note)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
    val background = noteBackground(note.colorIndex)
    val hasColor = note.colorIndex != 0
    val textColor = when {
        !hasColor -> MaterialTheme.colorScheme.onSurface
        isSystemInDarkTheme() -> Color(0xFFF2F2F2)
        else -> Color(0xFF1B1B1F)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (note.pinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (note.content.isNotBlank()) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Text(
            text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(note.updatedAt)),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditor(
    initial: Note?,
    onSave: (Note) -> Unit,
    onDelete: (() -> Unit)?,
    onClose: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(initial?.content ?: "") }
    var colorIndex by rememberSaveable { mutableIntStateOf(initial?.colorIndex ?: 0) }
    var pinned by rememberSaveable { mutableStateOf(initial?.pinned ?: false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // Saves when the user leaves the editor. Empty new notes are simply discarded.
    fun saveAndClose() {
        val isBlank = title.isBlank() && content.isBlank()
        if (!isBlank) {
            val changed = initial == null ||
                initial.title != title.trim() ||
                initial.content != content ||
                initial.colorIndex != colorIndex ||
                initial.pinned != pinned
            if (changed) {
                onSave(
                    (initial ?: Note()).copy(
                        title = title.trim(),
                        content = content,
                        colorIndex = colorIndex,
                        pinned = pinned,
                    ),
                )
            }
        }
        onClose()
    }

    BackHandler { saveAndClose() }

    val background = noteBackground(colorIndex)
    val dark = isSystemInDarkTheme()
    val textColor = when {
        colorIndex == 0 -> MaterialTheme.colorScheme.onSurface
        dark -> Color(0xFFF2F2F2)
        else -> Color(0xFF1B1B1F)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { saveAndClose() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save and go back", tint = textColor)
            }
            Text(
                text = if (initial == null) "New note" else "Edit note",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { pinned = !pinned }) {
                Icon(
                    imageVector = if (pinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (pinned) "Unpin" else "Pin",
                    tint = textColor,
                )
            }
            if (onDelete != null) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = textColor)
                }
            }
        }

        // Colour picker
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (index in 0 until LightNoteColors.size) {
                val swatch = noteBackground(index)
                val selected = index == colorIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .border(
                            width = if (selected) 2.5.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape,
                        )
                        .clickable { colorIndex = index },
                ) {
                    if (selected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        val transparentFields = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            cursorColor = textColor,
        )

        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = transparentFields,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("Start typing…") },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = transparentFields,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete note?") },
            text = { Text("This note will be removed for good.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                    onClose()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}
