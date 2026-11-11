package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Note
import com.example.ui.components.ActionButton
import com.example.ui.components.Eyebrow
import com.example.ui.components.Hairline
import com.example.ui.components.clickableCompat
import com.example.ui.components.toneFor
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.XasuusTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun SettingsDialog(
    notes: List<Note>,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    pinEnabled: Boolean,
    onPinChange: (String?) -> Unit,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val colors = XasuusTheme.colors
    val user = Firebase.auth.currentUser
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    val totalNotes = notes.count { !it.isArchived && !it.isDeleted }
    val pinnedCount = notes.count { it.isPinned && !it.isArchived && !it.isDeleted }
    val favoritesCount = notes.count { it.isFavorite && !it.isArchived && !it.isDeleted }
    val archivedCount = notes.count { it.isArchived && !it.isDeleted }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.card)
                .border(1.dp, colors.hairline, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(text = "Akoonka")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Habaynta",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.ink
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.recessed)
                        .clickableCompat(onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xir",
                        tint = colors.inkMuted,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ------------------------------------------------------------- profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.hairline, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.accent, colors.accent.copy(alpha = 0.65f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials(user?.displayName, user?.email),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(13.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.displayName ?: "Isticmaale Xasuus",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                            color = colors.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = user?.email ?: "Google account ku xiran",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = colors.inkFaint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ------------------------------------------------------------- sync status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentEmerald.copy(alpha = if (colors.isDark) 0.14f else 0.10f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald)
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Isku xirka cloud-ka wuu shaqeynayaa",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = if (colors.isDark) AccentEmerald else Color(0xFF177A50)
                        )
                        Text(
                            text = "Firestore · sync + offline cache",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.inkMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Eyebrow(text = "Tirakoobka")
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Description,
                        tint = colors.accent,
                        label = "Wadarta",
                        value = totalNotes
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PushPin,
                        tint = AccentAmber,
                        label = "La xushay",
                        value = pinnedCount
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite,
                        tint = AccentRose,
                        label = "Jeceshahay",
                        value = favoritesCount
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Archive,
                        tint = colors.inkMuted,
                        label = "Keydsan",
                        value = archivedCount
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Qufulka PIN-ka", style = MaterialTheme.typography.labelLarge, color = colors.ink)
                        Text(
                            if (pinEnabled) "App-ka wuu xirmayaa markaad ka baxdo" else "PIN 4 ilaa 8 lambar ah",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                    }
                    TextButton(onClick = { showPinDialog = true; newPin = ""; confirmPin = ""; pinError = null }) {
                        Text(if (pinEnabled) "Beddel" else "Deji", color = colors.accent)
                    }
                }
                if (pinEnabled) {
                    TextButton(onClick = { onPinChange(null) }, modifier = Modifier.align(Alignment.End)) {
                        Text("Dami qufulka PIN", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Muuqaalka mugdiga ah", style = MaterialTheme.typography.labelLarge, color = colors.ink)
                        Text(
                            "Doorashadaada way xasuusanaysaa",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                    }
                    Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
                }

                Spacer(modifier = Modifier.height(22.dp))

                Eyebrow(text = "Ku saabsan")
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.hairline, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    InfoRow("App", "Xasuus / MyNotes")
                    Spacer(modifier = Modifier.height(10.dp))
                    Hairline()
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Nooca", "1.0 · Paper & Ink")
                    Spacer(modifier = Modifier.height(10.dp))
                    Hairline()
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("Kaydinta", "Firebase Firestore")
                }

                Spacer(modifier = Modifier.height(22.dp))

                ActionButton(
                    text = "Ka bax akoonka",
                    icon = Icons.Default.Logout,
                    onClick = { showSignOutConfirm = true },
                    container = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                )
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = XasuusTheme.colors.card,
            title = { Text("Ka bax akoonka?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "Xasuusahaagu waxay ku sii kaydsan yihiin cloud-ka, waadna ku soo laaban kartaa marka aad mar kale gasho.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = XasuusTheme.colors.inkMuted
                )
            },
            confirmButton = {
                ActionButton(
                    text = "Haa, ka bax",
                    height = 44.dp,
                    onClick = {
                        showSignOutConfirm = false
                        onDismiss()
                        onSignOut()
                    },
                    container = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Jooji", color = XasuusTheme.colors.inkMuted)
                }
            }
        )
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.card,
            title = { Text("Deji PIN-ka Xasuus", color = colors.ink) },
            text = {
                Column {
                    Text("Dooro PIN u dhexeeya 4 iyo 8 lambar. Waxaa loo baahan yahay markaad app-ka dib ugu soo laabato.", color = colors.inkMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.filter(Char::isDigit).take(8); pinError = null },
                        label = { Text("PIN cusub") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it.filter(Char::isDigit).take(8); pinError = null },
                        label = { Text("Xaqiiji PIN-ka") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(pinError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                ActionButton(
                    text = "Kaydi PIN-ka",
                    height = 44.dp,
                    onClick = {
                        when {
                            newPin.length < 4 -> pinError = "PIN-ku ha ahaado ugu yaraan 4 lambar."
                            newPin != confirmPin -> pinError = "PIN-yadu isma waafaqaan."
                            else -> { onPinChange(newPin); showPinDialog = false }
                        }
                    },
                    container = colors.inkPill,
                    contentColor = colors.onInkPill
                )
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Ka noqo", color = colors.inkMuted) }
            }
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    value: Int
) {
    val colors = XasuusTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(colors.canvas)
            .border(1.dp, colors.hairline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            color = colors.ink
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = colors.inkFaint
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = XasuusTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
            color = colors.inkFaint
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
            color = colors.ink
        )
    }
}

private fun initials(name: String?, email: String?): String {
    val source = name?.trim()?.takeIf { it.isNotEmpty() } ?: email?.substringBefore("@").orEmpty()
    if (source.isEmpty()) return "X"
    val parts = source.split(" ", ".", "_", "-").filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        (parts[0].first().uppercaseChar().toString() + parts[1].first().uppercaseChar().toString())
    } else {
        source.take(2).uppercase()
    }
}
