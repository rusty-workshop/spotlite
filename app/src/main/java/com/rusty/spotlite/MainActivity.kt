package com.rusty.spotlite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.rusty.spotlite.ui.nav.Routes
import com.rusty.spotlite.ui.nav.SpotliteNavHost
import com.rusty.spotlite.ui.theme.SpotliteTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val container: AppContainer by lazy { (application as SpotliteApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleRedirectIfPresent(intent)

        setContent {
            var startDestination by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                startDestination = if (container.authRepository.isLoggedIn()) Routes.LIBRARY else Routes.LOGIN
            }

            SpotliteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    startDestination?.let { start ->
                        SpotliteNavHost(container = container, startDestination = start)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRedirectIfPresent(intent)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            // Only worth attempting once logged in — otherwise this always fails and
            // would show a "couldn't connect" banner on the login screen itself.
            if (container.authRepository.isLoggedIn()) {
                container.playbackController.connect()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        container.playbackController.disconnect()
    }

    private fun handleRedirectIfPresent(intent: Intent) {
        val uri: Uri = intent.data ?: return
        if (uri.scheme != "spotlite" || uri.host != "callback") return
        lifecycleScope.launch {
            container.authRepository.handleRedirect(uri)
            recreate()
        }
    }
}
