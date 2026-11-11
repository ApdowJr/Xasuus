package com.example.ui.notes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.ui.components.ActionButton
import com.example.ui.components.Eyebrow
import com.example.ui.components.Hairline
import com.example.ui.components.Pill
import com.example.ui.components.SoftIconButton
import com.example.ui.components.clickableCompat
import com.example.ui.components.toneFor
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.XasuusTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val AvailableColors = listOf(
    "purple", "blue", "teal", "emerald", "amber", "rose", "indigo", "slate"
)

/**
 * Editorial note editor.
 *
 * The writing surface is borderless: title in display type, body in comfortable body type,
 * and a quiet metadata block underneath. Everything visual (colour, category, tags, pin) is
 * a soft control that never competes with the text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    initialNote: Note?,
    categories: List<Category>,
    onBack: () -> Unit,
    onShare: (title: String, content: String, tags: List<String>) -> Unit,
    onSave: (
        title: String,
        content: String,
        categoryId: String,
        tags: List<String>,
        color: String,
        isPinned: Boolean,
        isFavorite: Boolean,
        isArchived: Boolean,
        reminderAtMillis: Long?
    ) -> Unit,
    onDelete: (() -> Unit)?
) {
    BackHandler { onBack() }

    val colors = XasuusTheme.colors

    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(initialNote?.categoryId ?: "general") }
    var selectedColor by remember { mutableStateOf(initialNote?.color ?: "purple") }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var isFavorite by remember { mutableStateOf(initialNote?.isFavorite ?: false) }
    var isArchived by remember { mutableStateOf(initialNote?.isArchived ?: false) }

    val tags = remember {
        mutableStateListOf<String>().apply {
            if (initialNote != null) addAll(initialNote.tags)
        }
    }
    var newTagInput by remember { mutableStateOf("") }
    var showTagDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMeta by remember { mutableStateOf(true) }
    var reminderAtMillis by remember { mutableStateOf(initialNote?.reminderAtMillis) }
    var reminderError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showAndroidReminderPicker(context) { selectedMillis ->
                if (selectedMillis > System.currentTimeMillis()) {
                    reminderAtMillis = selectedMillis
                    reminderError = null
                } else {
                    reminderError = "Fadlan dooro waqti mustaqbalka ah."
                }
            }
        } else {
            reminderError = "Ogeysiisyada waa la diiday. Ka daar Settings-ka telefoonka si xasuusintu u muuqato."
        }
    }
    val chooseReminder: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            reminderPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showAndroidReminderPicker(context) { selectedMillis ->
                if (selectedMillis > System.currentTimeMillis()) {
                    reminderAtMillis = selectedMillis
                    reminderError = null
                } else {
                    reminderError = "Fadlan dooro waqti mustaqbalka ah."
                }
            }
        }
    }

    val isNew = initialNote == null
    val tone = toneFor(selectedColor)
    val bodyFocus = remember { FocusRequester() }

    val timestampLabel = remember(initialNote?.updatedAt, initialNote?.createdAt) {
        val ts = initialNote?.updatedAt ?: initialNote?.createdAt
        if (ts != null) {
            "Waxaa la beddelay " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(ts.toDate())
        } else {
            "Xasuus cusub"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ----------------------------------------------------------------- top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Dib u noqo",
                    onClick = onBack,
                    size = 38.dp,
                    iconSize = 18.dp,
                    modifier = Modifier.testTag("note_editor_back_button")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(
                        text = if (isNew) "Xasuus cusub" else "Tafatir",
                        color = tone.accent
                    )
                    Text(
                        text = timestampLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.inkFaint
                    )
                }

                SoftIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "La wadaag xasuusta",
                    onClick = { onShare(title, content, tags.toList()) },
                    size = 36.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                ToggleIcon(
                    active = isPinned,
                    activeIcon = Icons.Filled.PushPin,
                    inactiveIcon = Icons.Outlined.PushPin,
                    activeTint = AccentAmber,
                    label = "Pin",
                    onClick = { isPinned = !isPinned },
                    modifier = Modifier.testTag("note_editor_pin_button")
                )
                Spacer(modifier = Modifier.width(6.dp))
                ToggleIcon(
                    active = isFavorite,
                    activeIcon = Icons.Filled.Favorite,
                    inactiveIcon = Icons.Filled.FavoriteBorder,
                    activeTint = AccentRose,
                    label = "Jecleysi",
                    onClick = { isFavorite = !isFavorite }
                )
                Spacer(modifier = Modifier.width(6.dp))
                SaveButton(
                    enabled = title.isNotBlank(),
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                title.trim(),
                                content.trim(),
                                selectedCategory,
                                tags.toList(),
                                selectedColor,
                                isPinned,
                                isFavorite,
                                isArchived,
                                reminderAtMillis
                            )
                        }
                    }
                )
            }

            Hairline(modifier = Modifier.padding(horizontal = 20.dp))

            // ----------------------------------------------------------------- writing area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .imePadding()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                BasicTextField(
                    value = title,
                    onValueChange = { title = it.replace("\n", "") },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    ),
                    cursorBrush = SolidColor(tone.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { bodyFocus.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                ) { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Cinwaanka xasuusta",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 28.sp,
                                lineHeight = 35.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.inkFaint.copy(alpha = 0.65f)
                        )
                    }
                    innerTextField()
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tone.accent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNew) "Qoraal cusub · maanta" else "La tafatiray",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = colors.inkFaint
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.5.sp,
                        lineHeight = 26.sp,
                        color = colors.ink
                    ),
                    cursorBrush = SolidColor(tone.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(bodyFocus)
                        .testTag("note_content_input")
                ) { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            text = "Bilow qorista… Fikrad, shaqo, ama wax xusuus leh.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.5.sp,
                                lineHeight = 26.sp
                            ),
                            color = colors.inkFaint.copy(alpha = 0.75f)
                        )
                    }
                    innerTextField()
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accentWash)
                        .clickableCompat(chooseReminder)
                        .padding(start = 13.dp, end = 8.dp, top = 11.dp, bottom = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (reminderAtMillis == null) "Xusuusin samee" else "Xusuusin la qorsheeyay",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.ink
                        )
                        Text(
                            text = reminderAtMillis?.let {
                                SimpleDateFormat("EEE, d MMM · HH:mm", Locale.getDefault()).format(java.util.Date(it))
                            } ?: "Ogeysiis waqtiga aad doorato",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.inkMuted
                        )
                    }
                    if (reminderAtMillis != null) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickableCompat { reminderAtMillis = null; reminderError = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ka saar xusuusinta",
                                tint = colors.inkMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                if (reminderError != null) {
                    Text(
                        text = reminderError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // --------------------------------------------------------- metadata panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Eyebrow(text = "Faahfaahin")
                    Text(
                        text = if (showMeta) "Qari" else "Muuji",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickableCompat { showMeta = !showMeta }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showMeta,
                    enter = fadeIn(tween(180)) + expandVertically(tween(200)),
                    exit = fadeOut(tween(120)) + shrinkVertically(tween(160))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Colour picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AvailableColors.forEach { key ->
                                val swatchTone = toneFor(key)
                                val selected = key == selectedColor
                                val ringScale by animateFloatAsState(
                                    targetValue = if (selected) 1f else 0f,
                                    animationSpec = tween(160),
                                    label = "swatch_ring"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchTone.wash)
                                        .border(
                                            width = (2f * ringScale).dp + 0.dp,
                                            color = if (selected) swatchTone.accent else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickableCompat { selectedColor = key },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (selected) 16.dp else 20.dp)
                                            .clip(CircleShape)
                                            .background(swatchTone.accent)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Eyebrow(text = "Qeybta")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val allCats = listOf("general" to "Guud") + categories.map { it.id to it.name }
                            allCats.forEach { (catId, catName) ->
                                val isSelected = selectedCategory.equals(catId, ignoreCase = true)
                                val catTone = toneFor(
                                    categories.firstOrNull { it.id == catId }?.color ?: "slate"
                                )
                                Pill(
                                    text = catName,
                                    selected = isSelected,
                                    tone = if (isSelected) catTone else null,
                                    onClick = { selectedCategory = catId }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Eyebrow(text = "Calaamadaha")
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickableCompat { showTagDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ku dar",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accent
                                )
                            }
                        }

                        if (tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                tags.forEach { tag ->
                                    Row(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(tone.wash)
                                            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = tone.accent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Tirtir tag",
                                            tint = tone.accent.copy(alpha = 0.7f),
                                            modifier = Modifier
                                                .size(13.dp)
                                                .clickableCompat { tags.remove(tag) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Pin + archive toggles
                        MetaToggleRow(
                            icon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            tint = AccentAmber,
                            title = "Ku dheji dusha sare",
                            subtitle = if (isPinned) "Xasuustani way ka sarraysaa liiska" else "Ka dhig mid ka muuqata dusha sare",
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            accent = AccentAmber,
                            testTag = "pin_note_toggle_switch"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        MetaToggleRow(
                            icon = if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                            tint = colors.inkMuted,
                            title = "Geli keydka",
                            subtitle = if (isArchived) "Waxay ku jirtaa keydka" else "Qari liiska guud, hayso keydka",
                            checked = isArchived,
                            onCheckedChange = { isArchived = it },
                            accent = colors.accent
                        )

                        if (!isNew && onDelete != null) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                    .clickableCompat { showDeleteConfirmDialog = true }
                                    .padding(vertical = 13.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = "Tirtir xasuusta",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ------------------------------------------------------------------ add tag dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = {
                showTagDialog = false
                newTagInput = ""
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.card,
            title = {
                Text(
                    text = "Ku dar calaamad",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Calaamaduhu waxay ku caawiyaan inaad si degdeg ah u hesho xasuusaha la xiriira.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.recessed)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        BasicTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                            cursorBrush = SolidColor(colors.accent),
                            modifier = Modifier.weight(1f)
                        )
                        if (newTagInput.isEmpty()) {
                            Text(
                                text = "shaqo, muhiim…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkFaint
                            )
                        }
                    }
                }
            },
            confirmButton = {
                ActionButton(
                    text = "Ku dar",
                    height = 44.dp,
                    onClick = {
                        val trimmed = newTagInput.trim().replace("#", "")
                        if (trimmed.isNotBlank() && tags.size < 10 && !tags.contains(trimmed)) {
                            tags.add(trimmed)
                        }
                        newTagInput = ""
                        showTagDialog = false
                    },
                    container = colors.accent,
                    contentColor = Color.White
                )
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Ka noqo", color = colors.inkMuted)
                }
            }
        )
    }

    // ------------------------------------------------------------------ delete confirm
    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.card,
            title = { Text("Tirtir xasuusta?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = "Xasuustan dib looma soo celin karo marka la tirtiro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkMuted
                )
            },
            confirmButton = {
                ActionButton(
                    text = "Haa, tirtir",
                    height = 44.dp,
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    container = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Ka noqo", color = colors.inkMuted)
                }
            }
        )
    }
}

@Composable
private fun ToggleIcon(
    active: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeTint: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = XasuusTheme.colors
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (active) activeTint.copy(alpha = 0.14f) else Color.Transparent)
            .clickableCompat(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (active) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = if (active) activeTint else colors.inkMuted,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.accent else colors.recessed)
            .clickableCompat { if (enabled) onClick() }
            .testTag("save_note_button")
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (enabled) Color.White else colors.inkFaint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "Kaydi",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) Color.White else colors.inkFaint
        )
    }
}

@Composable
private fun MetaToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color,
    testTag: String? = null
) {
    val colors = XasuusTheme.colors
    var modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(colors.card)
        .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
    if (testTag != null) {
        modifier = modifier.testTag(testTag)
    }
    Row(
        modifier = modifier
            .clickableCompat { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = colors.ink
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = colors.inkFaint
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.recessed,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

/** Native date/time pickers keep reminder input familiar and accessible. */
private fun showAndroidReminderPicker(context: Context, onSelected: (Long) -> Unit) {
    val now = java.util.Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, year)
                set(java.util.Calendar.MONTH, month)
                set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    selected.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                    selected.set(java.util.Calendar.MINUTE, minute)
                    selected.set(java.util.Calendar.SECOND, 0)
                    selected.set(java.util.Calendar.MILLISECOND, 0)
                    onSelected(selected.timeInMillis)
                },
                selected.get(java.util.Calendar.HOUR_OF_DAY),
                selected.get(java.util.Calendar.MINUTE),
                true
            ).show()
        },
        now.get(java.util.Calendar.YEAR),
        now.get(java.util.Calendar.MONTH),
        now.get(java.util.Calendar.DAY_OF_MONTH)
    )
    datePicker.datePicker.minDate = System.currentTimeMillis()
    datePicker.show()
}
