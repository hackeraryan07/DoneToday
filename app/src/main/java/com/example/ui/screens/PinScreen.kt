package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp

@Composable
fun PinScreen(
    title: String,
    onPinComplete: (String) -> Unit,
    errorMessage: String? = null
) {
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4
    
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp) {
                    when (event.key) {
                        Key.Backspace, Key.Delete -> {
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                            true
                        }
                        Key.Zero, Key.NumPad0 -> {
                            if (pin.length < maxPinLength) pin += "0"
                            true
                        }
                        Key.One, Key.NumPad1 -> {
                            if (pin.length < maxPinLength) pin += "1"
                            true
                        }
                        Key.Two, Key.NumPad2 -> {
                            if (pin.length < maxPinLength) pin += "2"
                            true
                        }
                        Key.Three, Key.NumPad3 -> {
                            if (pin.length < maxPinLength) pin += "3"
                            true
                        }
                        Key.Four, Key.NumPad4 -> {
                            if (pin.length < maxPinLength) pin += "4"
                            true
                        }
                        Key.Five, Key.NumPad5 -> {
                            if (pin.length < maxPinLength) pin += "5"
                            true
                        }
                        Key.Six, Key.NumPad6 -> {
                            if (pin.length < maxPinLength) pin += "6"
                            true
                        }
                        Key.Seven, Key.NumPad7 -> {
                            if (pin.length < maxPinLength) pin += "7"
                            true
                        }
                        Key.Eight, Key.NumPad8 -> {
                            if (pin.length < maxPinLength) pin += "8"
                            true
                        }
                        Key.Nine, Key.NumPad9 -> {
                            if (pin.length < maxPinLength) pin += "9"
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LaunchedEffect(pin) {
            if (pin.length == maxPinLength) {
                onPinComplete(pin)
                pin = ""
            }
        }
        
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
