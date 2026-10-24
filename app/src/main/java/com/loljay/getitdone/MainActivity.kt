package com.loljay.getitdone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loljay.getitdone.ui.theme.GetItDoneTheme
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetItDoneTheme {
                MainScreen(viewModel)
            }
        }
    }
}

private enum class Tab { FOCUS, HISTORY }

private enum class TaskFilter(val label: String) {
    ALL("All"),
    PERSONAL("Personal"),
    WORK("Work")
}

private const val FOCUS_LIMIT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var tab by rememberSaveable { mutableStateOf(Tab.FOCUS) }
    var filter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var editingTaskId by rememberSaveable { mutableLongStateOf(-1L) }
    var completingTaskId by rememberSaveable { mutableLongStateOf(-1L) }
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    val activeTasks = viewModel.tasks.filter { !it.isCompleted }
    val completedTasks = viewModel.tasks.filter { it.isCompleted }

    val visibleTasks = activeTasks
        .filter {
            when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.PERSONAL -> it.isPersonal
                TaskFilter.WORK -> !it.isPersonal
            }
        }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromId = from.key as? Long
        val toId = to.key as? Long
        if (fromId != null && toId != null) viewModel.moveTask(fromId, toId)
    }

    fun complete(task: TaskItem) {
        viewModel.completeTask(task.id)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "Well done — “${task.taskName}” is finished",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreTask(task.id)
        }
    }

    if (showCreateDialog) {
        TaskEditorDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                viewModel.addTask(it)
                showCreateDialog = false
            }
        )
    }

    val completingTask = viewModel.tasks.firstOrNull { it.id == completingTaskId && !it.isCompleted }
    if (completingTask != null) {
        CompleteTaskDialog(
            task = completingTask,
            onDismiss = { completingTaskId = -1L },
            onNextAction = { next ->
                viewModel.updateTask(completingTask.copy(nextActionName = next))
                completingTaskId = -1L
            },
            onTaskDone = {
                completingTaskId = -1L
                complete(completingTask)
            }
        )
    }

    val editingTask = viewModel.tasks.firstOrNull { it.id == editingTaskId }
    if (editingTask != null) {
        TaskEditorDialog(
            initialTask = editingTask,
            onDismiss = { editingTaskId = -1L },
            onConfirm = {
                viewModel.updateTask(it)
                editingTaskId = -1L
            },
            onDelete = {
                viewModel.deleteTask(editingTask.id)
                editingTaskId = -1L
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted “${editingTask.taskName}”",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.reinsertTask(editingTask)
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear history?") },
            text = { Text("This permanently removes all ${completedTasks.size} finished tasks.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (tab == Tab.FOCUS) "Get It Done" else "Finished work") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (tab == Tab.FOCUS) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.FOCUS,
                    onClick = { tab = Tab.FOCUS },
                    icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                    label = { Text("Focus") }
                )
                NavigationBarItem(
                    selected = tab == Tab.HISTORY,
                    onClick = { tab = Tab.HISTORY },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                    label = { Text("History (${completedTasks.size})") }
                )
            }
        }
    ) { innerPadding ->
        when (tab) {
            Tab.HISTORY -> HistoryScreen(
                completedTasks = completedTasks,
                onRestore = { viewModel.restoreTask(it.id) },
                onDelete = { viewModel.deleteTask(it.id) },
                onClear = { showClearConfirm = true },
                modifier = Modifier.padding(innerPadding)
            )

            Tab.FOCUS -> LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item(key = "filters") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskFilter.entries.forEach {
                            FilterChip(
                                selected = filter == it,
                                onClick = { filter = it },
                                label = { Text(it.label) }
                            )
                        }
                    }
                }

                if (!viewModel.isLoaded) {
                    // Wait for saved tasks before showing the empty state.
                } else if (visibleTasks.isEmpty()) {
                    item { EmptyFocusState(hasAnyActive = activeTasks.isNotEmpty(), hasHistory = completedTasks.isNotEmpty()) }
                } else {
                    item(key = "header") {
                        SectionHeader(
                            "Today's focus",
                            "Do first things first. The top $FOCUS_LIMIT are your focus. Press and hold a task to drag it into place."
                        )
                    }
                    itemsIndexed(visibleTasks, key = { _, task -> task.id }) { index, task ->
                        ReorderableItem(reorderState, key = task.id) { isDragging ->
                            TaskItemRow(
                                taskItem = task,
                                onComplete = { completingTaskId = task.id },
                                onClick = { editingTaskId = task.id },
                                isFocus = index < FOCUS_LIMIT,
                                isDragging = isDragging,
                                modifier = Modifier.longPressDraggableHandle(
                                    onDragStopped = { viewModel.commitOrder() }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyFocusState(hasAnyActive: Boolean, hasHistory: Boolean) {
    val message = when {
        hasAnyActive -> "No tasks match this filter."
        hasHistory -> "Everything is done. Rest well — receive today's rest as a gift, then add what's next when you're ready."
        else -> "Start with what matters. Tap + and capture one task with its very next action."
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    )
}
