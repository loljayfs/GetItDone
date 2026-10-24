package com.loljay.getitdone

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class TaskRepository(private val context: Context) {
    private val gson = Gson()
    private val fileName = "tasks.json"
    private val logTag = "TaskRepository"

    // Writes to a temp file first so a crash mid-write can't corrupt saved tasks.
    fun saveTasks(tasks: List<TaskItem>): Result<Unit> = runCatching<Unit> {
        val target = File(context.filesDir, fileName)
        val temp = File(context.filesDir, "$fileName.tmp")
        temp.writeText(gson.toJson(tasks))
        if (!temp.renameTo(target)) {
            target.delete()
            check(temp.renameTo(target)) { "Could not replace $fileName" }
        }
    }.onFailure { Log.e(logTag, "Failed to save tasks", it) }

    fun loadTasks(): Result<List<TaskItem>> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return Result.success(emptyList())

        return runCatching<List<TaskItem>> {
            val type = object : TypeToken<List<TaskItem>>() {}.type
            val loaded: List<TaskItem>? = gson.fromJson(file.readText(), type)
            // Older versions stored "no date" as 0.
            loaded.orEmpty().map { if (it.dueDate == 0L) it.copy(dueDate = null) else it }
        }.onFailure { Log.e(logTag, "Failed to load tasks", it) }
    }
}
