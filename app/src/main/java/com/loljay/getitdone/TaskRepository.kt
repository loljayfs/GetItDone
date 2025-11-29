package com.loljay.getitdone

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class TaskRepository(private val context: Context) {
    private val gson = Gson()
    private val fileName = "tasks.json"

    fun saveTasks(tasks: List<TaskItem>) {
        val file = File(context.filesDir, fileName)
        FileWriter(file).use { writer ->
            gson.toJson(tasks, writer)
        }
    }

    fun loadTasks(): List<TaskItem> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()
        
        return try {
            FileReader(file).use { reader ->
                val type = object : TypeToken<List<TaskItem>>() {}.type
                gson.fromJson(reader, type) ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
