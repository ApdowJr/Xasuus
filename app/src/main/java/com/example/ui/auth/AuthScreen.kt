package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.ActionButton
import com.example.ui.components.Eyebrow
import com.example.ui.components.SoftCard
import com.example.ui.components.clickableCompat
import com.example.ui.theme.XasuusTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class AuthMode {
    LOGIN,
    CREATE_ACCOUNT
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val colors = XasuusTheme.colors

    var selectedMode by remember { mutableStateOf(AuthMode.CREATE_ACCOUNT) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmVisible by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = colors.canvas) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(colors.accentWash, Color.Transparent)))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(colors.canvas)
                            .border(1.dp, colors.hairline, RoundedCornerShape(15.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.xasuus_logo),
                            contentDescription = "Astaanta Xasuus",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Xasuus",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = colors.ink
                        )
                        Text(
                            text = "Xasuusahaaga, meel ammaan ah.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Eyebrow(text = "KU SOO DHAWOOW")
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = if (selectedMode == AuthMode.CREATE_ACCOUNT) "Fikradahaaga meel u samee." else "Ku soo noqo Xasuus.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.ink
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = if (selectedMode == AuthMode.CREATE_ACCOUNT)
                            "Qor, habeey, oo meel kasta kala soco xasuusaha muhiimka kuu ah."
                        else "Gal koontadaada si aad u sii wadato xasuusahaaga.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.inkMuted
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Google remains an equal, prominent sign-in path without competing with the form.
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(15.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.accentWash),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(11.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Si fudud ugu gal Google",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.ink
                            )
                            Text(
                                text = "Hal taabasho, ammaan iyo degdeg",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.inkMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(13.dp))
                    ActionButton(
                        text = "Ku sii wad Google",
                        icon = Icons.Default.ArrowForward,
                        loading = isLoading,
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = { isLoading = false; onAuthSuccess() },
                                onAuthError = { err -> isLoading = false; errorMessage = err },
                                scope = scope,
                                onAuthCancelled = { isLoading = false }
                            )
                        },
                        enabled = !isLoading,
                        container = colors.inkPill,
                        contentColor = colors.onInkPill,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_sign_in_button")
                    )
                }

                Spacer(modifier = Modifier.height(19.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.hairline)
                    Text(
                        text = "AMA EMAIL",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                        color = colors.inkFaint
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = colors.hairline)
                }
                Spacer(modifier = Modifier.height(17.dp))

                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(17.dp)
                ) {
                    // Segmented selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.canvas)
                            .padding(4.dp)
                    ) {
                        AuthMode.entries.forEach { mode ->
                            val selected = selectedMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(if (selected) colors.inkPill else Color.Transparent)
                                    .clickableCompat {
                                        selectedMode = mode
                                        errorMessage = null
                                    }
                                    .testTag(if (mode == AuthMode.LOGIN) "tab_login" else "tab_create_account")
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (mode == AuthMode.LOGIN) "Galitaan" else "Akoon cusub",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) colors.onInkPill else colors.inkMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = selectedMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth-form"
                    ) { mode ->
                        Column {
                            if (mode == AuthMode.LOGIN) {
                                AuthField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it; errorMessage = null },
                                    label = "Email-kaaga",
                                    icon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    tag = "login_email_input"
                                )
                                Spacer(modifier = Modifier.height(11.dp))
                                AuthField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it; errorMessage = null },
                                    label = "Furaha sirta ah",
                                    icon = Icons.Default.Lock,
                                    keyboardType = KeyboardType.Password,
                                    password = true,
                                    passwordVisible = loginPasswordVisible,
                                    onTogglePassword = { loginPasswordVisible = !loginPasswordVisible },
                                    tag = "login_password_input"
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showForgotPassword = true }) {
                                        Text("Furaha ma illowday?", color = colors.accent)
                                    }
                                }
                                ActionButton(
                                    text = "Gal koontada",
                                    icon = Icons.Default.ArrowForward,
                                    onClick = {
                                        val email = loginEmail.trim()
                                        val pass = loginPassword.trim()
                                        if (email.isBlank() || !email.contains("@")) {
                                            errorMessage = "Fadlan geli email sax ah"
                                            return@ActionButton
                                        }
                                        if (pass.length < 6) {
                                            errorMessage = "Furaha sirta ah waa inuu ahaadaa ugu yaraan 6 xaraf"
                                            return@ActionButton
                                        }
                                        isLoading = true
                                        errorMessage = null
                                        performEmailLogin(
                                            email = email,
                                            pass = pass,
                                            scope = scope,
                                            onAuthSuccess = { isLoading = false; onAuthSuccess() },
                                            onAuthError = { err -> isLoading = false; errorMessage = err }
                                        )
                                    },
                                    enabled = !isLoading,
                                    loading = isLoading,
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("login_submit_button")
                                )
                            } else {
                                AuthField(
                                    value = registerName,
                                    onValueChange = { registerName = it; errorMessage = null },
                                    label = "Magacaaga oo buuxa",
                                    icon = Icons.Default.Person,
                                    tag = "register_name_input"
                                )
                                Spacer(modifier = Modifier.height(11.dp))
                                AuthField(
                                    value = registerEmail,
                                    onValueChange = { registerEmail = it; errorMessage = null },
                                    label = "Email-kaaga",
                                    icon = Icons.Default.Email,
                                    keyboardType = KeyboardType.Email,
                                    tag = "register_email_input"
                                )
                                Spacer(modifier = Modifier.height(11.dp))
                                AuthField(
                                    value = registerPassword,
                                    onValueChange = { registerPassword = it; errorMessage = null },
                                    label = "Furaha sirta ah",
                                    icon = Icons.Default.Lock,
                                    keyboardType = KeyboardType.Password,
                                    password = true,
                                    passwordVisible = registerPasswordVisible,
                                    onTogglePassword = { registerPasswordVisible = !registerPasswordVisible },
                                    tag = "register_password_input"
                                )
                                Spacer(modifier = Modifier.height(11.dp))
                                AuthField(
                                    value = registerConfirmPassword,
                                    onValueChange = { registerConfirmPassword = it; errorMessage = null },
                                    label = "Xaqiiji furaha sirta ah",
                                    icon = Icons.Default.Lock,
                                    keyboardType = KeyboardType.Password,
                                    password = true,
                                    passwordVisible = registerConfirmVisible,
                                    onTogglePassword = { registerConfirmVisible = !registerConfirmVisible },
                                    tag = "register_confirm_password_input"
                                )
                                Spacer(modifier = Modifier.height(17.dp))
                                ActionButton(
                                    text = "Abuur koonto",
                                    icon = Icons.Default.ArrowForward,
                                    onClick = {
                                        val name = registerName.trim()
                                        val email = registerEmail.trim()
                                        val pass = registerPassword.trim()
                                        val confirm = registerConfirmPassword.trim()
                                        when {
                                            name.isBlank() -> errorMessage = "Fadlan qor magacaaga"
                                            email.isBlank() || !email.contains("@") -> errorMessage = "Fadlan geli email sax ah"
                                            pass.length < 6 -> errorMessage = "Furaha sirta ah waa inuu ahaadaa ugu yaraan 6 xaraf"
                                            pass != confirm -> errorMessage = "Labada fure isma laha, fadlan hubi"
                                            else -> {
                                                isLoading = true
                                                errorMessage = null
                                                performEmailRegister(
                                                    name = name,
                                                    email = email,
                                                    pass = pass,
                                                    scope = scope,
                                                    onAuthSuccess = { isLoading = false; onAuthSuccess() },
                                                    onAuthError = { err -> isLoading = false; errorMessage = err }
                                                )
                                            }
                                        }
                                    },
                                    enabled = !isLoading,
                                    loading = isLoading,
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("register_submit_button")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = colors.inkFaint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Xasuusahaagu si ammaan ah ayay cloud-ka ugu kaydsan yihiin.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.inkFaint,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showForgotPassword) {
        ForgotPasswordDialog(
            initialEmail = loginEmail,
            scope = scope,
            onDismiss = { showForgotPassword = false }
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    tag: String
) {
    val colors = XasuusTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = colors.inkMuted) },
        trailingIcon = if (password) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Qari furaha" else "Muuji furaha",
                        tint = colors.inkMuted
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.hairline,
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.inkFaint,
            cursorColor = colors.accent,
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            focusedContainerColor = colors.card,
            unfocusedContainerColor = colors.card
        ),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    )
}

@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    scope: CoroutineScope,
    onDismiss: () -> Unit
) {
    val colors = XasuusTheme.colors
    var email by remember { mutableStateOf(initialEmail) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.card,
        title = { Text("Dib u deji furaha", color = colors.ink) },
        text = {
            Column {
                Text(
                    text = "Geli email-kaaga. Waxaan kuu soo diraynaa link aad ku samaysato fure cusub.",
                    color = colors.inkMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthField(
                    value = email,
                    onValueChange = { email = it; message = null },
                    label = "Email-kaaga",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    tag = "forgot_password_email_input"
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(9.dp))
                    Text(message.orEmpty(), color = colors.accent, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = email.trim()
                    if (clean.isBlank() || !clean.contains("@")) {
                        message = "Fadlan geli email sax ah"
                    } else {
                        loading = true
                        scope.launch {
                            try {
                                Firebase.auth.sendPasswordResetEmail(clean).await()
                                message = "Link-ga dib u dejinta waxaa loo diray $clean. Fadlan email-kaaga fiiri!"
                            } catch (e: Exception) {
                                message = e.localizedMessage ?: "Khalad ayaa dhacay"
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = colors.inkPill),
                shape = RoundedCornerShape(13.dp)
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(17.dp), color = colors.onInkPill, strokeWidth = 2.dp)
                else Text("Dir link-ga", color = colors.onInkPill)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Xir", color = colors.inkMuted) }
        }
    )
}
fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Habaynta Google Sign-In lama helin: default_web_client_id")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Nooca credential-ka lama aqoonsan")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Galitaanku ma guulaysan")
        }
    }
}

fun performSignOut(
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSignOutComplete: () -> Unit = {}
) {
    scope.launch {
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Failed to clear credential state: ${e.message}")
            } finally {
                Firebase.auth.signOut()
                onSignOutComplete()
            }
        }
    }
}
fun performEmailRegister(
    name: String,
    email: String,
    pass: String,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    scope.launch {
        try {
            val result = Firebase.auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null && name.isNotBlank()) {
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    Log.w("Auth", "Could not set displayName: ${e.message}")
                }
            }
            onAuthSuccess()
        } catch (e: Exception) {
            Log.e("Auth", "Registration failed", e)
            val friendlyMsg = when {
                e.message?.contains("already in use", ignoreCase = true) == true ||
                e.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                    "Email-kan hore ayaa loo isticmaalay. Fadlan ku gal tab-ka 'Galitaan' ama email kale isticmaal."
                e.message?.contains("weak password", ignoreCase = true) == true ||
                e.message?.contains("weak-password", ignoreCase = true) == true ->
                    "Furaha sirta ah waa mid daciif ah. Fadlan geli ugu yaraan 6 xaraf."
                e.message?.contains("badly formatted", ignoreCase = true) == true ||
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "Email-ka aad gelisay ma saxna."
                else -> e.localizedMessage ?: "Diiwaangalintu ma guulaysan, fadlan isku day markale"
            }
            onAuthError(friendlyMsg)
        }
    }
}

fun performEmailLogin(
    email: String,
    pass: String,
    scope: CoroutineScope,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    scope.launch {
        try {
            val credential = EmailAuthProvider.getCredential(email, pass)
            Firebase.auth.signInWithCredential(credential).await()
            onAuthSuccess()
        } catch (e: Exception) {
            Log.e("Auth", "Login failed", e)
            val friendlyMsg = when {
                e.message?.contains("no user record", ignoreCase = true) == true ||
                e.message?.contains("user-not-found", ignoreCase = true) == true ->
                    "Koontadan lama helin. Fadlan marka hore ku dhufo tab-ka 'Abuur Koonto'."
                e.message?.contains("invalid-credential", ignoreCase = true) == true ||
                e.message?.contains("wrong password", ignoreCase = true) == true ||
                e.message?.contains("wrong-password", ignoreCase = true) == true ->
                    "Email-ka ama furaha sirta ah ma saxna. Fadlan hubi."
                e.message?.contains("badly formatted", ignoreCase = true) == true ||
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "Email-ka aad gelisay ma saxna."
                else -> e.localizedMessage ?: "Galitaanku ma guulaysan, fadlan isku day markale"
            }
            onAuthError(friendlyMsg)
        }
    }
}
