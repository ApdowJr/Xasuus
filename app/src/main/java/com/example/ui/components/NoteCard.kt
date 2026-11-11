package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.XasuusTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Kept for backwards compatibility with older call sites: maps a stored colour key
 * to its strong accent colour.
 */
fun getNoteAccentColor(colorKey: String): Color = when (colorKey.lowercase()) {
    "purple" -> Color(0xFF6C4CF1)
    "blue" -> Color(0xFF3672E0)
    "teal" -> Color(0xFF0EA394)
    "emerald", "green" -> Color(0xFF23A26D)
    "amber", "orange" -> Color(0xFFE09A16)
    "rose", "red" -> Color(0xFFE44A73)
    "indigo" -> Color(0xFF5350D6)
    "slate", "gray", "grey" -> Color(0xFF6B7482)
    else -> Color(0xFF6C4CF1)
}

/** Friendly relative date: "Maanta" / "Shalay" / "12 Feb". */
fun formatNoteDate(note: Note): String {
    val timestamp = note.updatedAt ?: note.createdAt ?: return ""
    val date = timestamp.toDate()
    val now = java.util.Date()
    val dayMs = 24 * 60 * 60 * 1000L
    val todayStart = now.time - (now.time % dayMs)
    val noteStart = date.time - (date.time % dayMs)
    return when {
        noteStart >= todayStart -> "Maanta"
        noteStart >= todayStart - dayMs -> "Shalay"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
    }
}

/**
 * Editorial note card.
 *
 * Layout is a deliberate hierarchy: a tinted category strip at the top, the title in display
 * type, a generous preview of the body, then a footer with tags and quiet actions. Pinned
 * cards use a solid accent border and a pin badge so they read as "on top of the pile".
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showCategoryChip: Boolean = true,
    onRestore: () -> Unit = {},
    onDeleteForever: () -> Unit = {}
) {
    val colors = XasuusTheme.colors
    val tone = toneFor(note.color)
    val dateLabel = remember(note.updatedAt, note.createdAt) { formatNoteDate(note) }

    var menuOpen by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.978f else 1f,
        animationSpec = tween(130),
        label = "note_card_press"
    )
    val borderColor by animateColorAsState(
        targetValue = if (note.isPinned) tone.accent.copy(alpha = 0.55f) else colors.hairline,
        animationSpec = tween(220),
        label = "note_card_border"
    )

    Box(modifier = modifier.scale(scale)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.card)
                .border(if (note.isPinned) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(22.dp))
                .testTag("note_card_${note.id}")
                .pressable(
                    onPressChange = { pressed = it },
                    onClick = onClick,
                    onLongClick = { menuOpen = true }
                )
        ) {
            // ---------------------------------------------------------------- header strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 8.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.isPinned) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(tone.accent.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = null,
                                    tint = tone.accent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PIN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = tone.accent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (showCategoryChip) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(tone.accent)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = categoryLabel(note.categoryId),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = tone.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (note.isDeleted) {
                    Text(
                        text = "QASHIN",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.attachments.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "${note.attachments.size} lifaaq",
                            tint = colors.inkFaint,
                            modifier = Modifier.padding(end = 4.dp).size(13.dp)
                        )
                    }
                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Jeceshahay",
                            tint = AccentRose,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(13.dp)
                        )
                    }
                    if (note.reminderAtMillis != null) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Xusuusin la qorsheeyay",
                            tint = colors.accent,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(13.dp)
                        )
                    }
                    MiniAction(
                        icon = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        tint = if (note.isPinned) AccentAmber else colors.inkFaint,
                        contentDescription = if (note.isPinned) "Ka qaad pin" else "Pin",
                        onClick = onTogglePin
                    )
                    MiniAction(
                        icon = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        tint = if (note.isFavorite) AccentRose else colors.inkFaint,
                        contentDescription = "Jecleysi",
                        onClick = onToggleFavorite
                    )
                }
            }

            // ------------------------------------------------------------------------ body
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    text = note.title.ifBlank { "Xasuus aan cinwaan lahayn" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.ink,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note.content.trim(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = colors.inkMuted,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (note.checklist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tone.accent, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${note.checklist.count { it.isChecked }} / ${note.checklist.size} hawlood",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                    }
                }
            }

            // ---------------------------------------------------------------------- footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 12.dp, bottom = 12.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (note.tags.isNotEmpty()) {
                        note.tags.take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tone.wash)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = tone.accent,
                                    maxLines = 1
                                )
                            }
                        }
                        if (note.tags.size > 2) {
                            Text(
                                text = "+${note.tags.size - 2}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = colors.inkFaint
                            )
                        }
                    } else if (dateLabel.isNotEmpty()) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = colors.inkFaint
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dateLabel.isNotEmpty() && note.tags.isNotEmpty()) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = colors.inkFaint
                        )
                    }
                    MiniAction(
                        icon = androidx.compose.material.icons.Icons.Filled.MoreHoriz,
                        tint = colors.inkFaint,
                        contentDescription = "Ficillo dheeraad ah",
                        onClick = { menuOpen = true }
                    )
                }
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.hairline)
        ) {
            if (note.isDeleted) {
                DropdownMenuItem(
                    text = { Text("Soo celi xasuusta", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = null, tint = colors.accent) },
                    onClick = { menuOpen = false; onRestore() }
                )
                DropdownMenuItem(
                    text = { Text("Tirtir joogto ah", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { menuOpen = false; onDeleteForever() }
                )
            } else {
            DropdownMenuItem(
                text = { Text("Pin / Ka qaad pin", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    menuOpen = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text("Jecleysi", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = AccentRose,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    menuOpen = false
                    onToggleFavorite()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (note.isArchived) "Ka soo saar keydka" else "Geli keydka",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null,
                        tint = colors.inkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    menuOpen = false
                    onToggleArchive()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Tirtir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    menuOpen = false
                    onDelete()
                }
            )
            }
        }
    }
}

@Composable
private fun MiniAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .clickableCompat(onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
    }
}

internal fun categoryLabel(categoryId: String): String {
    if (categoryId.isBlank() || categoryId.equals("general", ignoreCase = true)) return "GUUD"
    return categoryId.replace('-', ' ').replace('_', ' ').uppercase()
}
