package com.loljay.getitdone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import com.loljay.getitdone.ui.theme.GetItDoneTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetItDoneTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = remember { TaskRepository(context) }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    val tasks = remember { mutableStateListOf<TaskItem>() }
    
    // Handle result from TaskDetailActivity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val action = result.data?.getStringExtra("TASK_ACTION")
            
            scope.launch(Dispatchers.IO) {
                if (action == "DELETE") {
                    val taskId = result.data?.getIntExtra("TASK_ID", -1)
                    if (taskId != -1) {
                        launch(Dispatchers.Main) {
                            tasks.removeAll { it.id == taskId }
                        }
                        repository.saveTasks(tasks.toList()) // Pass current list (will be updated on main thread shortly but safe to pass reference or wait? Better to wait)
                        // Actually tasks is a SnapshotStateList, accessing it in IO might be tricky if modifying in Main.
                        // Let's just save the modified list.
                        
                        // Better approach: modify list on Main, then save.
                        // The removeAll above happens on Main.
                        // We need to wait for it to happen or do it in one go.
                        
                        // Let's simplify:
                    }
                } else if (action == "UPDATE") {
                    val updatedTaskJson = result.data?.getStringExtra("UPDATED_TASK")
                    if (updatedTaskJson != null) {
                        val updatedTask = Gson().fromJson(updatedTaskJson, TaskItem::class.java)
                        launch(Dispatchers.Main) {
                            val index = tasks.indexOfFirst { it.id == updatedTask.id }
                            if (index != -1) {
                                tasks[index] = updatedTask
                            }
                        }
                    }
                }
                // Save changes to file
                launch(Dispatchers.Main) {
                     // Capture list on main thread to be safe
                     val currentList = tasks.toList()
                     launch(Dispatchers.IO) {
                         repository.saveTasks(currentList)
                     }
                }
            }
        }
    }
    
    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        tasks.apply {
            add(to.index, removeAt(from.index))
        }
    }, onDragEnd = { _, _ ->
         scope.launch(Dispatchers.IO) {
            repository.saveTasks(tasks.toList())
        }
    })
    
    // Load tasks initially
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val loadedTasks = repository.loadTasks()
            launch(Dispatchers.Main) {
                tasks.clear()
                tasks.addAll(loadedTasks)
            }
        }
    }

    // State to control the visibility of the AddTaskDialog
    var showAddTaskDialog by remember { mutableStateOf(false) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { newTask ->
                // Assign a simple ID based on list size + timestamp to avoid collision
                val taskWithId = newTask.copy(id = (tasks.size + System.currentTimeMillis()).toInt())
                tasks.add(taskWithId)
                
                // Save to file
                scope.launch(Dispatchers.IO) {
                    repository.saveTasks(tasks.toList())
                }
                
                showAddTaskDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Get It Done") },
                actions = {
                    IconButton(onClick = { showAddTaskDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Task"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            state = reorderState.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(reorderState)
                .detectReorderAfterLongPress(reorderState)
        ) {
            items(tasks, key = { it.id }) { task ->
                ReorderableItem(reorderableState = reorderState, key = task.id) { isDragging ->
                    Box(
                        modifier = Modifier.clickable {
                            val intent = Intent(context, TaskDetailActivity::class.java).apply {
                                putExtra("TASK_ITEM", Gson().toJson(task))
                            }
                            launcher.launch(intent)
                        }
                    ) {
                        TaskItemRow(taskItem = task)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GetItDoneTheme {
        MainScreen()
    }
}
