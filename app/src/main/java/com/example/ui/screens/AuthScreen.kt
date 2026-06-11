package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Done Today", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(8.dp))
        Text("Please sign in to continue", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                isLoading = true
                coroutineScope.launch {
                    try {
                        val authResult = doGoogleSignIn(context)
                        if (authResult) {
                            onAuthSuccess()
                        } else {
                            errorMessage = "Sign in failed"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Error: Check if Google Sign-In is enabled in Firebase Console, and SHA-1 is added. Ensure you have network connectivity. \n${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text("Sign in with Google")
            }
        }
        
        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

suspend fun doGoogleSignIn(context: Context): Boolean {
    val credentialManager = CredentialManager.create(context)
    
    // Fallback logic to get Web Client ID for Identity provider
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val webClientId = if (resId != 0) context.getString(resId) else {
        // If not found (due to missing OAuth client in google-services.json), we throw
        throw IllegalStateException("Web Client ID is missing. Add Google Sign-in OAuth client to Firebase.")
    }

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(context, request)
    return handleSignIn(result)
}

private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        return true
    }
    return false
}
