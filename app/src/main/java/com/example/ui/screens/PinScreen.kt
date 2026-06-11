package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PinScreen(
    title: String,
    onPinComplete: (String) -> Unit,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        // Pin Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until maxPinLength) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (i < pin.length) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(32.dp))

        // Numpad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in keys) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (key in row) {
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.size(80.dp))
                        } else if (key == "DEL") {
                            FilledTonalIconButton(
                                onClick = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Backspace, "Delete")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (pin.length < maxPinLength) {
                                        pin += key
                                        if (pin.length == maxPinLength) {
                                            onPinComplete(pin)
                                            pin = ""
                                        }
                                    }
                                },
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape
                            ) {
                                Text(key, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
