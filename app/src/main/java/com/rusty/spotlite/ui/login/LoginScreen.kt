package com.rusty.spotlite.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.auth.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(authRepository: AuthRepository, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.LibraryMusic,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Spotlite",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Your whole library, a fraction of the weight.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )
        Button(onClick = { scope.launch { authRepository.startLogin() } }) {
            Text("Log in with Spotify")
        }
    }
}
