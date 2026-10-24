package com.loljay.getitdone

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(application)
    private val saveMutex = Mutex()
    private var loadFailed = false

    val tasks = mutableStateListOf<TaskItem>()
    var isLoaded by mutableStateOf(false)
        private set

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.loadTasks() }
                .onSuccess { tasks.addAll(it) }
                .onFailure {
                    loadFailed = true
                    _messages.tryEmit("Could not load your tasks")
                }
            isLoaded = true
        }
    }

    fun addTask(task: TaskItem) {
        val nextId = maxOf(System.currentTimeMillis(), (tasks.maxOfOrNull { it.id } ?: 0L) + 1)
        tasks.add(task.copy(id = nextId, completedAt = null))
        persist()
    }

    fun updateTask(task: TaskItem) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            tasks[index] = task.copy(completedAt = tasks[index].completedAt)
            persist()
        }
    }

    fun completeTask(id: Long) = setCompleted(id, System.currentTimeMillis())

    fun restoreTask(id: Long) = setCompleted(id, null)

    fun deleteTask(id: Long) {
        if (tasks.removeAll { it.id == id }) persist()
    }

    fun reinsertTask(task: TaskItem) {
        if (tasks.none { it.id == task.id }) {
            tasks.add(task)
            persist()
        }
    }

    fun moveTask(fromId: Long, toId: Long) {
        val from = tasks.indexOfFirst { it.id == fromId }
        val to = tasks.indexOfFirst { it.id == toId }
        if (from >= 0 && to >= 0 && from != to) tasks.add(to, tasks.removeAt(from))
    }

    fun commitOrder() = persist()

    fun clearHistory() {
        if (tasks.removeAll { it.isCompleted }) persist()
    }

    private fun setCompleted(id: Long, completedAt: Long?) {
        val index = tasks.indexOfFirst { it.id == id }
        if (index >= 0) {
            tasks[index] = tasks[index].copy(completedAt = completedAt)
            persist()
        }
    }

    private fun persist() {
        // Never overwrite a file we failed to read; the user's data may still be recoverable.
        if (loadFailed) return
        val snapshot = tasks.toList()
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                repository.saveTasks(snapshot)
                    .onFailure { _messages.tryEmit("Could not save your tasks") }
            }
        }
    }
}
