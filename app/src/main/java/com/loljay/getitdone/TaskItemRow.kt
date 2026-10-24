package com.loljay.getitdone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.loljay.getitdone.ui.theme.GetItDoneTheme

@Composable
fun TaskItemRow(
    taskItem: TaskItem,
    onComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFocus: Boolean = false,
    isDragging: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 1.dp),
        border = if (isFocus) {
            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isFocus) {
                    Text(
                        text = "FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = taskItem.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Next: ${taskItem.nextActionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                taskItem.purpose?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TaskMeta(taskItem)
            }
            FilledIconButton(onClick = onComplete) {
                Icon(Icons.Filled.Check, contentDescription = "Mark ${taskItem.taskName} done")
            }
        }
    }
}

@Composable
fun TaskMeta(taskItem: TaskItem) {
    val due = taskItem.dueDate
    val overdue = due != null && isOverdue(due)
    val text = buildString {
        append(if (taskItem.isPersonal) "Personal" else "Work")
        if (due != null) append(" · ${if (overdue) "Overdue" else "Due"} ${formatDueDate(due)}")
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
fun TaskItemRowPreview() {
    GetItDoneTheme {
        TaskItemRow(
            taskItem = TaskItem(
                id = 1,
                taskName = "Write the project proposal",
                nextActionName = "Draft the outline",
                dueDate = todayAsUtcMillis(),
                purpose = "So the team can start with clarity"
            ),
            onComplete = {},
            onClick = {}
        )
    }
}
