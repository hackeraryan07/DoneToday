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

data class TodoItem(
    val id: String = "",
    val text: String = "",
    val scheduledDate: Long = 0L,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks = _tasks.asStateFlow()
    
    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos = _todos.asStateFlow()

    init {
        fetchTasks()
        fetchTodos()
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
    
    fun fetchTodos() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("todos")
            .orderBy("scheduledDate", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(TodoItem::class.java) }
                    _todos.value = list
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
    
    fun addTodo(text: String, scheduledDate: Long) {
        val user = auth.currentUser ?: return
        val id = UUID.randomUUID().toString()
        val todo = TodoItem(id = id, text = text, scheduledDate = scheduledDate, createdAt = System.currentTimeMillis())
        db.collection("users").document(user.uid).collection("todos").document(id).set(todo)
    }

    fun updateTodo(id: String, text: String, scheduledDate: Long) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("todos").document(id)
            .update(mapOf("text" to text, "scheduledDate" to scheduledDate))
    }

    fun markTodoCompleted(todo: TodoItem) {
        val user = auth.currentUser ?: return
        // Mark as completed
        db.collection("users").document(user.uid).collection("todos").document(todo.id)
            .update("completed", true)
        
        // Add to 'What I Did Today' (TaskItem)
        val taskId = UUID.randomUUID().toString()
        val task = TaskItem(id = taskId, text = todo.text, timestamp = System.currentTimeMillis())
        db.collection("users").document(user.uid).collection("tasks").document(taskId).set(task)
    }

    fun deleteTodo(id: String) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("todos").document(id).delete()
    }
}
