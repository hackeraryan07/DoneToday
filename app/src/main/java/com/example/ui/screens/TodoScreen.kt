package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.DateUtils
import com.example.MainViewModel
import com.example.TodoItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val allTodos by viewModel.todos.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var todoToComplete by remember { mutableStateOf<TodoItem?>(null) }
    var todoToEdit by remember { mutableStateOf<TodoItem?>(null) }
    
    // Filtering
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
    
    val pendingTodos = allTodos.filter { !it.completed && it.scheduledDate < todayStart }
    val todayTodos = allTodos.filter { it.scheduledDate in todayStart..todayEnd }
    val futureTodos = allTodos.filter { !it.completed && it.scheduledDate > todayEnd }
    val completedTodos = allTodos.filter { it.completed } // Optional to show

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Today", "Pending", "Future")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("To-Do List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Todo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            val currentList = when (selectedTab) {
                0 -> todayTodos.sortedBy { it.completed }
                1 -> pendingTodos.sortedBy { it.completed }
                else -> futureTodos.sortedBy { it.completed }
            }
            
            if (currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tasks here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList, key = { it.id }) { todo ->
                        TodoCard(
                            modifier = Modifier.animateItem(),
                            todo = todo,
                            onCompleteClick = if (todo.completed) null else { { todoToComplete = todo } },
                            onEditClick = if (todo.completed) null else { { todoToEdit = todo } },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TodoAddDialog(
            initialText = "",
            initialDateMillis = null,
            onDismiss = { showAddDialog = false },
            onSave = { text, date ->
                viewModel.addTodo(text, date)
                showAddDialog = false
            }
        )
    }

    if (todoToEdit != null) {
        TodoAddDialog(
            initialText = todoToEdit!!.text,
            initialDateMillis = todoToEdit!!.scheduledDate,
            onDismiss = { todoToEdit = null },
            onSave = { text, date ->
                viewModel.updateTodo(todoToEdit!!.id, text, date)
                todoToEdit = null
            }
        )
    }

    if (todoToComplete != null) {
        AlertDialog(
            onDismissRequest = { todoToComplete = null },
            title = { Text("Complete Task") },
            text = { Text("Are you sure you want to mark this as completed? It will be saved to what you have done today.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.markTodoCompleted(todoToComplete!!)
                    todoToComplete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { todoToComplete = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun TodoCard(
    modifier: Modifier = Modifier,
    todo: TodoItem,
    onCompleteClick: (() -> Unit)?,
    onEditClick: (() -> Unit)?,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (todo.completed) 0.5f else 1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.text,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                )
                Text(
                    text = "Scheduled: ${DateUtils.formatDate(todo.scheduledDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                )
            }
            if (onCompleteClick != null) {
                IconButton(onClick = onCompleteClick) {
                    Icon(Icons.Default.Check, contentDescription = "Complete", tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (onEditClick != null) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun TodoAddDialog(
    initialText: String = "",
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    val context = LocalContext.current
    
    val calendar = Calendar.getInstance()
    if (initialDateMillis != null) {
        calendar.timeInMillis = initialDateMillis
    } else {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }
    
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            selectedDateMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialText.isEmpty()) "New To-Do" else "Edit To-Do") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedCard(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(Modifier.width(16.dp))
                        Text("Date: ${DateUtils.formatDate(selectedDateMillis)}")
                    }
                }
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter task...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text, selectedDateMillis) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
