package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Type system.
 *
 * - Display / headline: Plus Jakarta Sans (geometric, editorial, slightly tighter tracking)
 * - Body / label: Manrope (open, highly legible at small sizes)
 *
 * Both families are bundled as static weights in res/font so the app never depends on
 * downloadable fonts (works offline and inside previews/tests).
 */

val JakartaSans = FontFamily(
    Font(R.font.jakarta_400, FontWeight.Normal),
    Font(R.font.jakarta_500, FontWeight.Medium),
    Font(R.font.jakarta_600, FontWeight.SemiBold),
    Font(R.font.jakarta_700, FontWeight.Bold),
    Font(R.font.jakarta_800, FontWeight.ExtraBold),
)

val ManropeSans = FontFamily(
    Font(R.font.manrope_400, FontWeight.Normal),
    Font(R.font.manrope_500, FontWeight.Medium),
    Font(R.font.manrope_600, FontWeight.SemiBold),
    Font(R.font.manrope_700, FontWeight.Bold),
    Font(R.font.manrope_800, FontWeight.ExtraBold),
)

private val trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

private fun display(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Bold,
    letterSpacing: Double = -0.8
) = TextStyle(
    fontFamily = JakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = trim
)

private fun body(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = ManropeSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = trim
)

val Typography = Typography(
    // Editorial display sizes (splash, auth hero, section titles)
    displayLarge = display(44, 50, FontWeight.ExtraBold, -1.4),
    displayMedium = display(38, 44, FontWeight.ExtraBold, -1.2),
    displaySmall = display(32, 38, FontWeight.Bold, -1.0),

    headlineLarge = display(30, 36, FontWeight.Bold, -0.9),
    headlineMedium = display(26, 32, FontWeight.Bold, -0.7),
    headlineSmall = display(22, 28, FontWeight.Bold, -0.5),

    titleLarge = display(20, 26, FontWeight.Bold, -0.4),
    titleMedium = display(17, 23, FontWeight.Bold, -0.2),
    titleSmall = display(15, 20, FontWeight.SemiBold, -0.1),

    bodyLarge = body(16, 25),
    bodyMedium = body(14, 22),
    bodySmall = body(12.5f.toInt(), 18),

    labelLarge = body(13, 18, FontWeight.SemiBold, 0.1),
    labelMedium = body(12, 16, FontWeight.SemiBold, 0.2),
    labelSmall = body(11, 15, FontWeight.SemiBold, 0.4),
)
