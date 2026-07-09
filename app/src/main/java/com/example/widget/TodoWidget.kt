package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.example.TodoItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

import androidx.compose.ui.graphics.Color
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import android.content.ComponentName

class TodoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val user = Firebase.auth.currentUser
        val todos = mutableListOf<TodoItem>()
        if (user != null) {
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1

            try {
                val snapshot = Firebase.firestore.collection("users").document(user.uid).collection("todos")
                    .whereGreaterThanOrEqualTo("scheduledDate", todayStart)
                    .whereLessThanOrEqualTo("scheduledDate", todayEnd)
                    .get()
                    .await()
                todos.addAll(snapshot.documents.mapNotNull { it.toObject(TodoItem::class.java) }.filter { !it.completed })
            } catch (e: Exception) {
                // Ignore
            }
        }

        provideContent {
            val tabKey = ActionParameters.Key<String>("tab")
            val actionParams = actionParametersOf(tabKey to "todo")
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ColorProvider(Color(0xFFE8DEF8))) // Material3 light primary container
                    .cornerRadius(16.dp)
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>(actionParams))
            ) {
                Text(
                    text = "Today's To-Do",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF1D192B)) // onPrimaryContainer
                    )
                )
                Spacer(modifier = GlanceModifier.height(12.dp))
                if (todos.isEmpty()) {
                    Text(
                        text = "No tasks for today",
                        style = TextStyle(color = ColorProvider(Color(0xFF49454F))) // onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                        items(todos) { todo ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•",
                                    style = TextStyle(fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF6750A4)))
                                )
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = todo.text,
                                    style = TextStyle(color = ColorProvider(Color(0xFF1D192B))),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
