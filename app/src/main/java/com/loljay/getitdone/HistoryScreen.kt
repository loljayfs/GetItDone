package com.loljay.getitdone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000

@Composable
fun HistoryScreen(
    completedTasks: List<TaskItem>,
    onRestore: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sorted = completedTasks.sortedByDescending { it.completedAt ?: 0L }
    val thisWeek = sorted.count { (it.completedAt ?: 0L) >= System.currentTimeMillis() - WEEK_MILLIS }
    val grouped = sorted.groupBy { formatLocalDate(it.completedAt ?: 0L) }

    if (sorted.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nothing finished yet", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Completed tasks will appear here so you can look back with gratitude on the faithful work you've done.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$thisWeek done in the last 7 days",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Review the week: what was worth the effort, and what should you stop or delegate?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        grouped.forEach { (day, tasks) ->
            item(key = "header-$day") {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(tasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
                        Text(task.taskName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Finished with: ${task.nextActionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        task.purpose?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { onRestore(task) }) { Text("Restore") }
                            TextButton(onClick = { onDelete(task) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = onClear,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) { Text("Clear history") }
        }
    }
}
