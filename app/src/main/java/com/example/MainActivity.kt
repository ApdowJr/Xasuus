package com.example

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.security.AppPinStore
import com.example.ui.auth.AuthScreen
import com.example.ui.main.MainScreen
import com.example.ui.security.AppLockScreen
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
    private var lockOnStop: (() -> Unit)? = null
    private var openNoteId by mutableStateOf<String?>(null)
    private var createNoteFromWidget by mutableStateOf(false)
    private var openDeepLinkRequestId by mutableStateOf(0)

    override fun onStop() {
        super.onStop()
        lockOnStop?.invoke()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeDeepLinks(intent)
    }

    private fun consumeDeepLinks(intent: Intent?) {
        intent?.getStringExtra("open_note_id")?.takeIf { it.isNotBlank() }?.let { id ->
            openNoteId = id
            openDeepLinkRequestId += 1
            intent.removeExtra("open_note_id")
        }
        if (intent?.getBooleanExtra("create_note_from_widget", false) == true) {
            createNoteFromWidget = true
            intent.removeExtra("create_note_from_widget")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeDeepLinks(intent)
        setContent {
            val preferences = remember { getSharedPreferences("xasuus_preferences", MODE_PRIVATE) }
            val systemDarkTheme = isSystemInDarkTheme()
            var darkTheme by remember {
                mutableStateOf(preferences.getBoolean("dark_theme", systemDarkTheme))
            }
            var pinEnabled by remember { mutableStateOf(AppPinStore.isEnabled(this@MainActivity)) }
            var isAppLocked by remember { mutableStateOf(pinEnabled) }
            SideEffect {
                lockOnStop = { if (pinEnabled) isAppLocked = true }
            }
            MyNotesTheme(darkTheme = darkTheme) {
                if (pinEnabled && isAppLocked) {
                    AppLockScreen(onUnlocked = { isAppLocked = false })
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppContent(
                            openCreateNote = createNoteFromWidget,
                            openNoteId = openNoteId,
                            openNoteRequestId = openDeepLinkRequestId,
                            onOpenNoteHandled = { handledId ->
                                if (openNoteId == handledId) openNoteId = null
                            },
                            onCreateNoteHandled = { createNoteFromWidget = false },
                            pinEnabled = pinEnabled,
                            onPinChange = { pin ->
                                AppPinStore.setPin(this@MainActivity, pin)
                                pinEnabled = !pin.isNullOrBlank()
                                isAppLocked = pinEnabled
                            },
                            onThemeChange = { enabled ->
                                darkTheme = enabled
                                preferences.edit().putBoolean("dark_theme", enabled).apply()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppContent(
    openCreateNote: Boolean = false,
    openNoteId: String? = null,
    openNoteRequestId: Int = 0,
    onOpenNoteHandled: (String) -> Unit = {},
    onCreateNoteHandled: () -> Unit = {},
    pinEnabled: Boolean = false,
    onPinChange: (String?) -> Unit = {},
    onThemeChange: (Boolean) -> Unit = {}
) {
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
                        MainScreen(
                            currentUserId = user.uid,
                            openCreateNote = openCreateNote,
                            openNoteId = openNoteId,
                            openNoteRequestId = openNoteRequestId,
                            onOpenNoteHandled = onOpenNoteHandled,
                            onCreateNoteHandled = onCreateNoteHandled,
                            pinEnabled = pinEnabled,
                            onPinChange = onPinChange,
                            onThemeChange = onThemeChange
                        )
                    }
                } else {
                    currentDestination = AppDestination.AUTH
                }
            }
        }
    }
}
