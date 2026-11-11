package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.XasuusTheme

// ---------------------------------------------------------------------------------------------
// Category tone palette — maps note/category colour keys to a coherent pastel system.
// ---------------------------------------------------------------------------------------------

/**
 * A three-part tone: a strong accent (dots, icons, text), a soft wash (card + chip background)
 * and a deeper shade used for hairlines and the card footer rail.
 */
data class Tone(
    val accent: Color,
    val wash: Color,
    val deep: Color
)

@Composable
fun toneFor(colorKey: String): Tone {
    val dark = XasuusTheme.colors.isDark

    fun light(accent: Long, wash: Long, deep: Long) =
        Tone(Color(accent), Color(wash), Color(deep))

    fun dark(accent: Long, wash: Long) =
        Tone(Color(accent), Color(wash), Color(accent).copy(alpha = 0.45f))

    return when (colorKey.lowercase()) {
        "blue" -> if (dark) dark(0xFF7FA8FF, 0xFF182338) else light(0xFF3672E0, 0xFFE7F0FF, 0xFFCFE1FF)
        "teal" -> if (dark) dark(0xFF52D6C9, 0xFF132B2A) else light(0xFF0EA394, 0xFFDFF6F3, 0xFFBFEDE7)
        "emerald", "green" -> if (dark) dark(0xFF5FD79E, 0xFF13291F) else light(0xFF23A26D, 0xFFE2F7EB, 0xFFC6EFD9)
        "amber", "orange" -> if (dark) dark(0xFFF5C15C, 0xFF2C2313) else light(0xFFE09A16, 0xFFFDF1DA, 0xFFF8DEA9)
        "rose", "red" -> if (dark) dark(0xFFFF8FA9, 0xFF2E1720) else light(0xFFE44A73, 0xFFFFE8EF, 0xFFFFCEDA)
        "indigo" -> if (dark) dark(0xFF9C99FF, 0xFF1D1C36) else light(0xFF5350D6, 0xFFE9E9FF, 0xFFD2D2FF)
        "slate", "gray", "grey" -> if (dark) dark(0xFFB3BBC9, 0xFF1E2128) else light(0xFF6B7482, 0xFFEFF1F5, 0xFFDDE1E8)
        else -> if (dark) dark(0xFFA79AFF, 0xFF241F3D) else light(0xFF6C4CF1, 0xFFEFEAFF, 0xFFDCD2FF)
    }
}

// ---------------------------------------------------------------------------------------------
// Surfaces
// ---------------------------------------------------------------------------------------------

/**
 * The app's workhorse card: white (near-black in dark mode), 24dp corners, hairline border and
 * no heavy drop shadow — depth comes from the border against the warm paper canvas.
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    color: Color = XasuusTheme.colors.card,
    borderColor: Color = XasuusTheme.colors.hairline,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .clip(shape)
        .background(color)
        .border(borderWidth, borderColor, shape)
    val clickable = if (onClick != null) {
        base.clickable(interactionSource = null, indication = null, onClick = onClick)
    } else base

    Column(modifier = clickable.padding(contentPadding), content = content)
}

/** Small uppercase eyebrow label used above section titles. */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = XasuusTheme.colors.inkFaint
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** Rounded pill used for filters, tags and small status chips. */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: ImageVector? = null,
    tone: Tone? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
) {
    val colors = XasuusTheme.colors
    val container = when {
        selected && tone != null -> tone.wash
        selected -> colors.inkPill
        else -> Color.Transparent
    }
    val contentColor = when {
        selected && tone != null -> tone.accent
        selected -> colors.onInkPill
        else -> colors.inkMuted
    }
    val borderColor = when {
        selected && tone != null -> tone.accent.copy(alpha = 0.35f)
        selected -> Color.Transparent
        else -> colors.hairline
    }

    val base = modifier
        .clip(CircleShape)
        .background(container)
        .border(1.dp, borderColor, CircleShape)
    val clickable = if (onClick != null) {
        base.clickable(interactionSource = null, indication = null, onClick = onClick)
    } else base

    Row(
        modifier = clickable.padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1
        )
    }
}

/** Bold primary action matching the ink/violet language of the app. */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    container: Color = XasuusTheme.colors.inkPill,
    contentColor: Color = XasuusTheme.colors.onInkPill,
    height: Dp = 52.dp
) {
    Surface(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick
            ),
        color = container,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}

/** Circular icon button with soft press feedback. */
@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 19.dp,
    container: Color = XasuusTheme.colors.card,
    borderColor: Color = XasuusTheme.colors.hairline,
    tint: Color = XasuusTheme.colors.ink
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(120),
        label = "soft_icon_press"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Thin hairline divider that respects the canvas palette. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = XasuusTheme.colors.hairline) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/** Loading skeleton block with the shared sweep shimmer. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    corner: Dp = 8.dp,
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(brush)
    )
}

// ---------------------------------------------------------------------------------------------
// Interactions
// ---------------------------------------------------------------------------------------------

/** Click with no ripple (the app uses scale feedback instead). */
fun Modifier.clickableCompat(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = null,
    indication = null,
    onClick = onClick
)

/**
 * Press-aware clickable that reports press state so the caller can animate scale,
 * and fires a long-press callback for context menus.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.pressable(
    onPressChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier = this.composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    androidx.compose.runtime.LaunchedEffect(pressed) { onPressChange(pressed) }
    this.combinedClickable(
        interactionSource = interaction,
        indication = null,
        onClick = onClick,
        onLongClick = onLongClick
    )
}
