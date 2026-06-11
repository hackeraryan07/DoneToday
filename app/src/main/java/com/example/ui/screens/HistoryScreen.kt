package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.DateUtils
import com.example.MainViewModel
import com.example.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val allTasks by viewModel.tasks.collectAsState()
    val historyTasks = remember(allTasks) { allTasks.filter { !DateUtils.isToday(it.timestamp) } }
    val groupedTasks = remember(historyTasks) { historyTasks.groupBy { DateUtils.formatDate(it.timestamp) } }

    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (groupedTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "You don't have any older tasks saved.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedTasks.forEach { (dateStr, dateTasks) ->
                    item(key = dateStr) {
                        val isExpanded = expandedDates.contains(dateStr)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            onClick = {
                                expandedDates = if (isExpanded) {
                                    expandedDates - dateStr
                                } else {
                                    expandedDates + dateStr
                                }
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(dateStr, style = MaterialTheme.typography.titleMedium)
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand/Collapse"
                                    )
                                }
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        dateTasks.forEach { task ->
                                            TaskCard(task, onEdit = {
                                                taskToEdit = task
                                            }, onDelete = {
                                                viewModel.deleteTask(task.id)
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (taskToEdit != null) {
        TaskDialog(
            initialText = taskToEdit!!.text,
            onDismiss = { taskToEdit = null },
            onSave = { text ->
                viewModel.updateTask(taskToEdit!!.id, text)
                taskToEdit = null
            }
        )
    }
}
