package com.loljay.getitdone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskItemRow(taskItem: TaskItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = taskItem.taskName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = taskItem.nextActionName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier
                    .background(
                        color = Color(0xFFDCEDC8), // Light Green
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal =1.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Row {
                if (taskItem.dueDate != 0L) {
                    val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(taskItem.dueDate))
                    Text(
                        text = "Due: $dateString",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemRowPreview() {
    TaskItemRow(
        taskItem = TaskItem(
            id = 1,
            taskName = "sample task",
            nextActionName = "complete the documentation",
            dueDate = System.currentTimeMillis(),
            goalType = GoalType.SHORT_TERM,
            isPersonal = true
        )
    )
}
