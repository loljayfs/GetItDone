package com.loljay.getitdone

data class TaskItem(
    val id: Int,
    val taskName: String,
    val nextActionName: String,
    val dueDate: Long,
    val goalType: GoalType,
    val isPersonal: Boolean,
    val notes: String? = null
)

enum class GoalType {
    SHORT_TERM,
    LONG_TERM
}
