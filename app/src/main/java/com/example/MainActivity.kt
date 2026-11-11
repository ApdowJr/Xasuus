package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.auth.AuthScreen
import com.example.ui.main.MainScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyNotesTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class AppDestination {
    SPLASH,
    AUTH,
    MAIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            currentUser = user
            if (user == null && currentDestination == AppDestination.MAIN) {
                currentDestination = AppDestination.AUTH
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "app_navigation_content"
    ) { destination ->
        when (destination) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onSessionChecked = { isAuthenticated, _ ->
                        val user = Firebase.auth.currentUser
                        if (isAuthenticated && user != null) {
                            currentUser = user
                            currentDestination = AppDestination.MAIN
                        } else {
                            currentDestination = AppDestination.AUTH
                        }
                    }
                )
            }
            AppDestination.AUTH -> {
                AuthScreen(
                    onAuthSuccess = {
                        currentUser = Firebase.auth.currentUser
                        currentDestination = AppDestination.MAIN
                    }
                )
            }
            AppDestination.MAIN -> {
                val user = currentUser ?: Firebase.auth.currentUser
                if (user != null) {
                    key(user.uid) {
                        MainScreen(currentUserId = user.uid)
                    }
                } else {
                    currentDestination = AppDestination.AUTH
                }
            }
        }
    }
}
