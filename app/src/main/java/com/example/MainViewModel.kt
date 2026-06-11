package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class TaskItem(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks = _tasks.asStateFlow()

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("tasks")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(TaskItem::class.java) }
                    _tasks.value = list
                }
            }
    }

    fun addTask(text: String) {
        val user = auth.currentUser ?: return
        val id = UUID.randomUUID().toString()
        val task = TaskItem(id = id, text = text, timestamp = System.currentTimeMillis())
        db.collection("users").document(user.uid).collection("tasks").document(id).set(task)
    }

    fun updateTask(id: String, text: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("tasks").document(id)
            .update("text", text)
    }
    
    fun deleteTask(id: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("tasks").document(id).delete()
    }
}
