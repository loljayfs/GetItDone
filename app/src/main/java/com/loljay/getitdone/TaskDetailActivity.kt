package com.loljay.getitdone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.loljay.getitdone.ui.theme.GetItDoneTheme

class TaskDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val taskJson = intent.getStringExtra("TASK_ITEM")
        val initialTaskItem = if (taskJson != null) {
            Gson().fromJson(taskJson, TaskItem::class.java)
        } else {
            finish()
            return
        }

        setContent {
            GetItDoneTheme {
                var currentTaskItem by remember { mutableStateOf(initialTaskItem) }

                TaskDetailScreen(
                    taskItem = currentTaskItem,
                    onBack = { 
                        val resultIntent = Intent().apply {
                            putExtra("TASK_ACTION", "UPDATE")
                            putExtra("UPDATED_TASK", Gson().toJson(currentTaskItem))
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish() 
                    },
                    onComplete = {
                        val resultIntent = Intent().apply {
                            putExtra("TASK_ACTION", "DELETE")
                            putExtra("TASK_ID", currentTaskItem.id)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    onTaskUpdated = { updatedTask ->
                        currentTaskItem = updatedTask
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskItem: TaskItem,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onTaskUpdated: (TaskItem) -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showNextActionDialog by remember { mutableStateOf(false) }
    var showEditNotesDialog by remember { mutableStateOf(false) }
    var nextActionInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Task") },
            text = { Text("Are you sure you want to complete this task?") },
            confirmButton = {
                TextButton(onClick = onComplete) {
                    Text("Sure")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNextActionDialog) {
        AlertDialog(
            onDismissRequest = { showNextActionDialog = false },
            title = { Text("Update Next Action") },
            text = {
                OutlinedTextField(
                    value = nextActionInput,
                    onValueChange = { nextActionInput = it },
                    label = { Text("Next Action") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nextActionInput.isNotBlank()) {
                            onTaskUpdated(taskItem.copy(nextActionName = nextActionInput))
                            showNextActionDialog = false
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextActionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showEditNotesDialog) {
        AlertDialog(
            onDismissRequest = { showEditNotesDialog = false },
            title = { Text("Edit Notes") },
            text = {
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes") },
                    minLines = 3,
                    maxLines = 8
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTaskUpdated(taskItem.copy(notes = if (notesInput.isBlank()) null else notesInput))
                        showEditNotesDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(taskItem.taskName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                     IconButton(onClick = { 
                         notesInput = taskItem.notes ?: ""
                         showEditNotesDialog = true
                     }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Notes")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                 Text(
                    text = taskItem.nextActionName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .background(
                            color = Color(0xFFDCEDC8),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            nextActionInput = taskItem.nextActionName
                            showNextActionDialog = true
                        }
                        .padding(16.dp)
                        .fillMaxWidth()
                )
                
                if (!taskItem.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Notes:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                     Text(
                        text = taskItem.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        nextActionInput = ""
                        showNextActionDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC80), contentColor = Color.Black)
                ) {
                    Text("Move to Next Action")
                }

                Button(
                    onClick = { showCompleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8E6C9), contentColor = Color.Black)
                ) {
                    Text("Task Completed")
                }
            }
        }
    }
}
