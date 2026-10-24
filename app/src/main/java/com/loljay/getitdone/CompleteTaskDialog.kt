package com.loljay.getitdone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CompleteTaskDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onNextAction: (String) -> Unit,
    onTaskDone: () -> Unit
) {
    var nextAction by rememberSaveable { mutableStateOf("") }
    val canMoveOn = nextAction.isNotBlank()

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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nice work", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "You finished “${task.nextActionName}”. What's next for “${task.taskName}”?",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text("Next action") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onNextAction(nextAction.trim()) },
                    enabled = canMoveOn,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Move to next action") }
                OutlinedButton(
                    onClick = onTaskDone,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("The whole task is done") }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }
            }
        }
    }
}
