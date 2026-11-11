package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.VibrantViolet
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
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedMode by remember { mutableStateOf(AuthMode.CREATE_ACCOUNT) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Register Form State
    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Brand Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricPurple, VibrantViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "MyNotes Icon",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MyNotes",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Xasuus-qor Casri ah oo Ammaan ah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Fast Google 1-Tap Option Card (Always available at the top for immediate access)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElectricPurple.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Habka Tooska ah (Google 1-Taabasho)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ku gal ama ku abuur koonto hal taabasho oo toos ugu xiran Firebase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_sign_in_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricPurple
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Ku Gal Google Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Or Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  AMA BUUXI FOOMKA  ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Card with Tabs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Tab Row: Galitaan (Login) vs Abuur Koonto (Register)
                    TabRow(
                        selectedTabIndex = selectedMode.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = ElectricPurple,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedMode.ordinal]),
                                color = ElectricPurple
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedMode == AuthMode.LOGIN,
                            onClick = {
                                selectedMode = AuthMode.LOGIN
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Galitaan (Login)",
                                    fontWeight = if (selectedMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_login")
                        )
                        Tab(
                            selected = selectedMode == AuthMode.CREATE_ACCOUNT,
                            onClick = {
                                selectedMode = AuthMode.CREATE_ACCOUNT
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "Abuur Koonto",
                                    fontWeight = if (selectedMode == AuthMode.CREATE_ACCOUNT) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_create_account")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error Message
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (selectedMode == AuthMode.LOGIN) {
                        // --- LOGIN FORM ---
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = {
                                loginEmail = it
                                errorMessage = null
                            },
                            label = { Text("Email-ka") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                errorMessage = null
                            },
                            label = { Text("Furaha Sirta ah (Password)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ElectricPurple)
                                )
                                Text(
                                    text = "I xasuuso",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Ha iloobin furaha?",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ElectricPurple,
                                modifier = Modifier
                                    .clickable { showForgotPasswordDialog = true }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val cleanEmail = loginEmail.trim()
                                val cleanPass = loginPassword.trim()
                                if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                    errorMessage = "Fadlan geli email sax ah"
                                    return@Button
                                }
                                if (cleanPass.length < 6) {
                                    errorMessage = "Furaha sirta ah waa inuu ahaadaa ugu yaraan 6 xaraf"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                performEmailLogin(
                                    email = cleanEmail,
                                    pass = cleanPass,
                                    scope = coroutineScope,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Gal (Sign In)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                    } else {
                        // --- CREATE ACCOUNT FORM ---
                        OutlinedTextField(
                            value = registerName,
                            onValueChange = {
                                registerName = it
                                errorMessage = null
                            },
                            label = { Text("Magacaaga oo Buuxa") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerEmail,
                            onValueChange = {
                                registerEmail = it
                                errorMessage = null
                            },
                            label = { Text("Email-kaaga") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerPassword,
                            onValueChange = {
                                registerPassword = it
                                errorMessage = null
                            },
                            label = { Text("Furaha Sirta ah (Password)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { registerPasswordVisible = !registerPasswordVisible }) {
                                    Icon(
                                        imageVector = if (registerPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (registerPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registerConfirmPassword,
                            onValueChange = {
                                registerConfirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("Xaqiiji Furaha Sirta ah") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ElectricPurple
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (registerConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (registerConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val cleanName = registerName.trim()
                                val cleanEmail = registerEmail.trim()
                                val cleanPass = registerPassword.trim()
                                val cleanConfirm = registerConfirmPassword.trim()

                                if (cleanName.isBlank()) {
                                    errorMessage = "Fadlan qor magacaaga"
                                    return@Button
                                }
                                if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                                    errorMessage = "Fadlan geli email sax ah"
                                    return@Button
                                }
                                if (cleanPass.length < 6) {
                                    errorMessage = "Furaha sirta ah waa inuu ahaadaa ugu yaraan 6 xaraf"
                                    return@Button
                                }
                                if (cleanPass != cleanConfirm) {
                                    errorMessage = "Labada fure isma laha, fadlan hubi"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                performEmailRegister(
                                    name = cleanName,
                                    email = cleanEmail,
                                    pass = cleanPass,
                                    scope = coroutineScope,
                                    onAuthSuccess = {
                                        isLoading = false
                                        onAuthSuccess()
                                    },
                                    onAuthError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Abuur Koontada (Register)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Xogtaadu waxay ku kaydsan tahay Cloud Firestore si ammaan ah",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(loginEmail) }
        var resetMessage by remember { mutableStateOf<String?>(null) }
        var resetLoading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Dib u Dejinta Furaha Sirta ah") },
            text = {
                Column {
                    Text(
                        text = "Geli email-kaaga si aan kuugu soo dirno link-ga aad ku beddesho furaha sirta ah:"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email-kaaga") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = resetMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricPurple
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = resetEmail.trim()
                        if (clean.isBlank() || !clean.contains("@")) {
                            resetMessage = "Fadlan geli email sax ah"
                            return@Button
                        }
                        resetLoading = true
                        coroutineScope.launch {
                            try {
                                Firebase.auth.sendPasswordResetEmail(clean).await()
                                resetLoading = false
                                resetMessage = "Link-ga dib u dejinta waxaa loo diray $clean. Fadlan email-kaaga fiiri!"
                            } catch (e: Exception) {
                                resetLoading = false
                                resetMessage = e.localizedMessage ?: "Khalad ayaa dhacay"
                            }
                        }
                    },
                    enabled = !resetLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    if (resetLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Dir Link-ga")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Xir")
                }
            }
        )
    }
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
