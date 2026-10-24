package com.loljay.getitdone

data class TaskItem(
    val id: Long,
    val taskName: String,
    val nextActionName: String,
    val dueDate: Long? = null,
    val goalType: GoalType? = GoalType.SHORT_TERM,
    val isPersonal: Boolean = true,
    val purpose: String? = null,
    val notes: String? = null,
    val completedAt: Long? = null
) {
    val isCompleted: Boolean get() = completedAt != null
}

enum class GoalType {
    SHORT_TERM,
    LONG_TERM
}
