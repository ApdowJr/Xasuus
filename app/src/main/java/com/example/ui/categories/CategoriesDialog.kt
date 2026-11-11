package com.example.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Category
import com.example.ui.components.ActionButton
import com.example.ui.components.Eyebrow
import com.example.ui.components.Pill
import com.example.ui.components.SoftCard
import com.example.ui.components.clickableCompat
import com.example.ui.components.toneFor
import com.example.ui.theme.XasuusTheme

private val CategoryColorOptions = listOf("purple", "blue", "teal", "emerald", "amber", "rose", "indigo", "slate")

/**
 * Category manager, presented as a rounded bottom-anchored sheet instead of a stock alert
 * dialog so it matches the rest of the app's paper-and-pill language.
 */
@Composable
fun CategoriesDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCreateCategory: (name: String, color: String, icon: String) -> Unit,
    onDeleteCategory: (categoryId: String) -> Unit
) {
    val colors = XasuusTheme.colors
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("purple") }
    val tone = toneFor(selectedColor)

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
            // --------------------------------------------------------------- header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(text = "Maktabadda", color = tone.accent)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Maamul qeybaha",
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

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ----------------------------------------------------------- creator card
                SoftCard(
                    color = tone.wash,
                    borderColor = Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    Eyebrow(text = "Qeyb cusub", color = tone.accent)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = if (colors.isDark) 0.08f else 0.75f))
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(tone.accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = tone.accent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (newCategoryName.isEmpty()) {
                                Text(
                                    text = "Magaca qeybta, tusaale: Shaqo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.inkFaint
                                )
                            }
                            BasicTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                                cursorBrush = SolidColor(tone.accent),
                                modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Eyebrow(text = "Midab", color = colors.inkMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        CategoryColorOptions.forEach { colorKey ->
                            val swatch = toneFor(colorKey)
                            val selected = selectedColor == colorKey
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(swatch.accent)
                                    .border(
                                        width = if (selected) 2.dp else 0.dp,
                                        color = if (selected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickableCompat { selectedColor = colorKey },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    ActionButton(
                        text = "Ku dar qeybta",
                        icon = Icons.Default.Add,
                        height = 44.dp,
                        enabled = newCategoryName.isNotBlank(),
                        container = tone.accent,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_category_button"),
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                onCreateCategory(newCategoryName.trim(), selectedColor, "folder")
                                newCategoryName = ""
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Eyebrow(text = "Qeybaha jira")
                    Pill(
                        text = categories.size.toString(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 9.dp,
                            vertical = 3.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (categories.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = colors.inkFaint,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Weli wax qeyb gaar ah ma aadan abuurin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            val catTone = toneFor(cat.color)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.canvas)
                                    .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
                                    .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(catTone.wash),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FolderOpen,
                                        contentDescription = null,
                                        tint = catTone.accent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                                        color = colors.ink,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Qeybta xasuusaha",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = colors.inkFaint
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .clickableCompat { onDeleteCategory(cat.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Tirtir",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            ActionButton(
                text = "Waan fahmay",
                onClick = onDismiss,
                container = colors.recessed,
                contentColor = colors.ink,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
