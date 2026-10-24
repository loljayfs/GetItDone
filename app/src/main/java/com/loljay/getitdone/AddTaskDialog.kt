package com.loljay.getitdone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    initialTask: TaskItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (TaskItem) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isEditing = initialTask != null
    var taskName by rememberSaveable { mutableStateOf(initialTask?.taskName ?: "") }
    var nextAction by rememberSaveable { mutableStateOf(initialTask?.nextActionName ?: "") }
    var purpose by rememberSaveable { mutableStateOf(initialTask?.purpose ?: "") }
    var notes by rememberSaveable { mutableStateOf(initialTask?.notes ?: "") }
    var dueDate by rememberSaveable { mutableStateOf(initialTask?.dueDate) }
    var isPersonal by rememberSaveable { mutableStateOf(initialTask?.isPersonal ?: true) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = pickerState.selectedDateMillis
                        showDatePicker = false
                    },
                    enabled = pickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?") },
            text = { Text("“${initialTask?.taskName.orEmpty()}” will be removed without being added to your history.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    fun submit() {
        if (taskName.isBlank() || nextAction.isBlank()) {
            showErrors = true
            return
        }
        onConfirm(
            TaskItem(
                id = initialTask?.id ?: 0L,
                taskName = taskName.trim(),
                nextActionName = nextAction.trim(),
                dueDate = dueDate,
                goalType = initialTask?.goalType ?: GoalType.SHORT_TERM,
                isPersonal = isPersonal,
                purpose = purpose.trim().ifBlank { null },
                notes = notes.trim().ifBlank { null },
                completedAt = initialTask?.completedAt
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(max = 640.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit task" else "New task",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("What do you want to get done?") },
                    isError = showErrors && taskName.isBlank(),
                    supportingText = { if (showErrors && taskName.isBlank()) Text("Required") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("Next physical action") },
                    placeholder = { Text("The very next step, e.g. “Call Sam”") },
                    isError = showErrors && nextAction.isBlank(),
                    supportingText = { if (showErrors && nextAction.isBlank()) Text("Required") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Why it matters (optional)") },
                    placeholder = { Text("Who does this serve?") },
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dueDate?.let { formatDueDate(it) } ?: "No due date",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(if (dueDate == null) "Set date" else "Change")
                    }
                    if (dueDate != null) {
                        TextButton(onClick = { dueDate = null }) { Text("Clear") }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isPersonal,
                        onClick = { isPersonal = true },
                        label = { Text("Personal") }
                    )
                    FilterChip(
                        selected = !isPersonal,
                        onClick = { isPersonal = false },
                        label = { Text("Work") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing && onDelete != null) {
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = ::submit) { Text(if (isEditing) "Save" else "Add") }
                }
            }
        }
    }
}
