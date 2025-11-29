package com.loljay.getitdone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var nextActionName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(0L) }
    var isPersonal by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var taskNameError by remember { mutableStateOf(false) }
    var nextActionError by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        dueDate = datePickerState.selectedDateMillis ?: 0L
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add New Task",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { 
                        taskName = it
                        if (taskNameError && it.isNotBlank()) taskNameError = false
                    },
                    label = { Text("Task Name") },
                    isError = taskNameError,
                    supportingText = { if (taskNameError) Text("Task name required") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextActionName,
                    onValueChange = { 
                        nextActionName = it
                        if (nextActionError && it.isNotBlank()) nextActionError = false
                    },
                    label = { Text("Next Action") },
                    isError = nextActionError,
                    supportingText = { if (nextActionError) Text("Next action required") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 7,
                    maxLines = 100
                )

                // Date Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (dueDate == 0L) "No Date Selected" else SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(
                            Date(dueDate)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Select Date")
                    }
                }

                // Type Selection
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isPersonal,
                        onClick = { isPersonal = true }
                    )
                    Text("Personal")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = !isPersonal,
                        onClick = { isPersonal = false }
                    )
                    Text("Work")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val isTaskNameValid = taskName.isNotBlank()
                            val isNextActionValid = nextActionName.isNotBlank()
                            
                            if (isTaskNameValid && isNextActionValid) {
                                onConfirm(
                                    TaskItem(
                                        id = 0, 
                                        taskName = taskName,
                                        nextActionName = nextActionName,
                                        dueDate = dueDate,
                                        goalType = GoalType.SHORT_TERM,
                                        isPersonal = isPersonal,
                                        notes = notes.ifBlank { null }
                                    )
                                )
                            } else {
                                taskNameError = !isTaskNameValid
                                nextActionError = !isNextActionValid
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
