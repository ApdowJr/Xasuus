package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------------------------
// Material 3 colour schemes
// ---------------------------------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = BrandVioletLight,
    onPrimary = Color(0xFF14121C),
    primaryContainer = Color(0xFF2B2547),
    onPrimaryContainer = SoftLavender,
    secondary = VibrantViolet,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = SoftLavender,
    tertiary = AccentCyan,
    onTertiary = Color(0xFF06211F),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurfaceAlt,
    surfaceContainerHigh = DarkSurfaceVariant,
    surfaceContainerLow = DarkSurface,
    outline = DarkBorder,
    outlineVariant = Color(0xFF262230),
    error = Color(0xFFFF7A8F),
    onError = Color(0xFF2B0710),
    errorContainer = Color(0xFF3D1420),
    onErrorContainer = Color(0xFFFFD9DF),
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandViolet,
    onPrimary = Color.White,
    primaryContainer = BrandVioletSoft,
    onPrimaryContainer = BrandVioletDark,
    secondary = Ink,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Ink,
    tertiary = AccentCyan,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurfaceAlt,
    surfaceContainerHigh = LightSurfaceVariant,
    surfaceContainerLow = LightSurface,
    outline = LightBorder,
    outlineVariant = Color(0xFFF3F1EC),
    error = Color(0xFFD92D4B),
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E8),
    onErrorContainer = Color(0xFF7C0C22),
    scrim = Color(0xFF16151A)
)

// ---------------------------------------------------------------------------------------------
// Extended design tokens (things Material 3 doesn't model)
// ---------------------------------------------------------------------------------------------

@Immutable
data class XasuusColors(
    /** Page background. */
    val canvas: Color,
    /** Card / sheet background. */
    val card: Color,
    /** Slightly recessed surface (search fields, chips, tab rails). */
    val recessed: Color,
    /** Hairline border for cards and inputs. */
    val hairline: Color,
    /** Strong hairline for focused/selected elements. */
    val hairlineStrong: Color,
    /** Primary ink for headings. */
    val ink: Color,
    /** Secondary text. */
    val inkMuted: Color,
    /** Tertiary text (timestamps, hints). */
    val inkFaint: Color,
    /** Accent colour for selected navigation / primary actions. */
    val accent: Color,
    /** Soft accent wash behind icons and pills. */
    val accentWash: Color,
    /** Dark "pill" surface used for the floating tab bar. */
    val inkPill: Color,
    val onInkPill: Color,
    /** Card elevation shadow colour. */
    val shadow: Color,
    val isDark: Boolean
)

val LightXasuusColors = XasuusColors(
    canvas = LightBg,
    card = LightSurface,
    recessed = LightSurfaceVariant,
    hairline = LightBorder,
    hairlineStrong = Color(0xFFD8D4CB),
    ink = LightTextPrimary,
    inkMuted = LightTextSecondary,
    inkFaint = LightTextTertiary,
    accent = BrandViolet,
    accentWash = BrandVioletSoft,
    inkPill = Ink,
    onInkPill = Color(0xFFF7F6F9),
    shadow = Color(0x14000000),
    isDark = false
)

val DarkXasuusColors = XasuusColors(
    canvas = DarkBg,
    card = DarkSurface,
    recessed = DarkSurfaceVariant,
    hairline = DarkBorder,
    hairlineStrong = Color(0xFF3B3649),
    ink = DarkTextPrimary,
    inkMuted = DarkTextSecondary,
    inkFaint = DarkTextTertiary,
    accent = BrandVioletLight,
    accentWash = Color(0xFF2A2344),
    inkPill = Color(0xFFF2F0F7),
    onInkPill = Color(0xFF141218),
    shadow = Color(0x66000000),
    isDark = true
)

val LocalXasuusColors = staticCompositionLocalOf { LightXasuusColors }

/** Convenience accessor: `XasuusTheme.colors.accent`. */
object XasuusTheme {
    val colors: XasuusColors
        @Composable get() = LocalXasuusColors.current

    val shapes: Shapes
        @Composable get() = MaterialTheme.shapes
}

/** Small travel/airiness scale so cards, sheets and pills feel consistent. */
val XasuusShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

// ---------------------------------------------------------------------------------------------
// Theme entry points
// ---------------------------------------------------------------------------------------------

@Composable
fun MyNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extended = if (darkTheme) DarkXasuusColors else LightXasuusColors

    CompositionLocalProvider(LocalXasuusColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = XasuusShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = MyNotesTheme(darkTheme, dynamicColor, content)

/** Text style helper so screens can use display type without Material's colour defaults. */
fun TextStyle.inkColor(color: Color): TextStyle = copy(color = color)
