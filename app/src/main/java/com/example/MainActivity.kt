package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val pinState = AppSettings.getPin(this).collectAsStateWithLifecycle(initialValue = "LOADING")
                    
                    if (pinState.value == "LOADING") {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDest = if (pinState.value == null) "setup_pin" else "unlock_pin"
                        
                        NavHost(navController = navController, startDestination = startDest) {
                            composable("setup_pin") {
                                val scope = rememberCoroutineScope()
                                PinScreen(
                                    title = "Set App PIN",
                                    onPinComplete = { newPin ->
                                        scope.launch {
                                            AppSettings.setPin(this@MainActivity, newPin)
                                            val nextDest = if (Firebase.auth.currentUser == null) "auth" else "home"
                                            navController.navigate(nextDest) {
                                                popUpTo("setup_pin") { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }
                            composable("unlock_pin") {
                                var errorMsg by remember { mutableStateOf<String?>(null) }
                                PinScreen(
                                    title = "Enter PIN to Unlock",
                                    errorMessage = errorMsg,
                                    onPinComplete = { entered ->
                                        if (entered == pinState.value) {
                                            val nextDest = if (Firebase.auth.currentUser == null) "auth" else "home"
                                            navController.navigate(nextDest) {
                                                popUpTo("unlock_pin") { inclusive = true }
                                            }
                                        } else {
                                            errorMsg = "Incorrect PIN"
                                        }
                                    }
                                )
                            }
                            composable("auth") {
                                AuthScreen(onAuthSuccess = {
                                    viewModel.fetchTasks()
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                })
                            }
                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateHistory = {
                                        navController.navigate("history")
                                    }
                                )
                            }
                            composable("history") {
                                com.example.ui.screens.HistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
