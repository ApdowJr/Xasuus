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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    val colors = XasuusTheme.colors
    val user = Firebase.auth.currentUser
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val totalNotes = notes.count { !it.isArchived }
    val pinnedCount = notes.count { it.isPinned && !it.isArchived }
    val favoritesCount = notes.count { it.isFavorite && !it.isArchived }
    val archivedCount = notes.count { it.isArchived }

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
