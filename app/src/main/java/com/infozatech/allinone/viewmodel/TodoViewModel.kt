package com.infozatech.allinone.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.infozatech.allinone.data.AppDatabase
import com.infozatech.allinone.data.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    DONE("Done"),
}

class TodoViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).todoDao()

    private val _filter = MutableStateFlow(TodoFilter.ALL)
    val filter: StateFlow<TodoFilter> = _filter.asStateFlow()

    val allTodos: StateFlow<List<Todo>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val visibleTodos: StateFlow<List<Todo>> = combine(allTodos, _filter) { list, filter ->
        when (filter) {
            TodoFilter.ALL -> list
            TodoFilter.ACTIVE -> list.filter { !it.done }
            TodoFilter.DONE -> list.filter { it.done }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(filter: TodoFilter) {
        _filter.value = filter
    }

    /** Adds a new task (id = 0) or updates an existing one. */
    fun save(todo: Todo) {
        viewModelScope.launch { dao.upsert(todo) }
    }

    fun toggle(todo: Todo) = save(todo.copy(done = !todo.done))

    fun delete(todo: Todo) {
        viewModelScope.launch { dao.delete(todo) }
    }

    fun clearCompleted() {
        viewModelScope.launch { dao.deleteCompleted() }
    }
}
