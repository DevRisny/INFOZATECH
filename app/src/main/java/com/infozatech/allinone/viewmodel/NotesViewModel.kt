package com.infozatech.allinone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.infozatech.allinone.data.AppDatabase
import com.infozatech.allinone.data.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).noteDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** The note being edited: null = list is showing, [NEW_NOTE] = a blank new note. */
    private val _editingId = MutableStateFlow<Long?>(null)
    val editingId: StateFlow<Long?> = _editingId.asStateFlow()

    val allNotes: StateFlow<List<Note>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Notes filtered by the search box (matches the title, and also the content). */
    val visibleNotes: StateFlow<List<Note>> = combine(allNotes, _query) { notes, q ->
        val text = q.trim()
        if (text.isEmpty()) {
            notes
        } else {
            notes.filter {
                it.title.contains(text, ignoreCase = true) ||
                    it.content.contains(text, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun openEditor(id: Long) {
        _editingId.value = id
    }

    fun closeEditor() {
        _editingId.value = null
    }

    fun save(note: Note) {
        viewModelScope.launch { dao.upsert(note.copy(updatedAt = System.currentTimeMillis())) }
    }

    fun delete(note: Note) {
        viewModelScope.launch { dao.delete(note) }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch { dao.upsert(note.copy(pinned = !note.pinned)) }
    }

    companion object {
        const val NEW_NOTE = -1L
    }
}
