package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Xasuus design system — "Paper & Ink".
 *
 * A warm-paper background, pure white cards with hairline borders, near-black ink for text
 * and a single confident violet accent. Category colours are paired with soft pastel tints
 * so note cards read like coloured index cards instead of flat white boxes.
 *
 * Legacy names (ElectricPurple, VibrantViolet, Accent*) are kept as aliases so every screen
 * keeps compiling against one source of truth.
 */

// ---------------------------------------------------------------------------------------------
// Brand
// ---------------------------------------------------------------------------------------------

/** Primary brand ink-violet. */
val BrandViolet = Color(0xFF5A45E8)
val BrandVioletDark = Color(0xFF4534C4)
val BrandVioletLight = Color(0xFF8B7BF7)
val BrandVioletSoft = Color(0xFFEDE9FF)

/** Legacy aliases used across older screens. */
val ElectricPurple = BrandViolet
val ElectricPurpleLight = BrandVioletLight
val ElectricPurpleDark = BrandVioletDark
val VibrantViolet = Color(0xFF7C63F2)
val SoftLavender = Color(0xFFDCD4FF)
val PalePurple = BrandVioletSoft

/** Near-black ink used for FABs, primary buttons and headlines. */
val Ink = Color(0xFF16151A)
val InkSoft = Color(0xFF26242C)
val InkMuted = Color(0xFF3A3743)

// ---------------------------------------------------------------------------------------------
// Accents
// ---------------------------------------------------------------------------------------------

val AccentCyan = Color(0xFF16A6A0)
val AccentAmber = Color(0xFFE9A020)
val AccentEmerald = Color(0xFF23A26D)
val AccentRose = Color(0xFFE44A73)
val AccentBlue = Color(0xFF3672E0)

// ---------------------------------------------------------------------------------------------
// Light theme surfaces
// ---------------------------------------------------------------------------------------------

val LightBg = Color(0xFFF7F6F3)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceAlt = Color(0xFFFBFAF8)
val LightSurfaceVariant = Color(0xFFF1EFEA)
val LightBorder = Color(0xFFE9E6DF)
val LightTextPrimary = Color(0xFF16151A)
val LightTextSecondary = Color(0xFF6B6875)
val LightTextTertiary = Color(0xFF9B98A4)

// ---------------------------------------------------------------------------------------------
// Dark theme surfaces ("midnight ink")
// ---------------------------------------------------------------------------------------------

val DarkBg = Color(0xFF0D0C11)
val DarkSurface = Color(0xFF17161D)
val DarkSurfaceAlt = Color(0xFF1D1B25)
val DarkSurfaceVariant = Color(0xFF252231)
val DarkBorder = Color(0xFF302C3C)
val DarkTextPrimary = Color(0xFFF4F2F7)
val DarkTextSecondary = Color(0xFFA8A4B6)
val DarkTextTertiary = Color(0xFF7A7688)

// ---------------------------------------------------------------------------------------------
// Category accent colours (strong tones, for dots/icons/text)
// ---------------------------------------------------------------------------------------------

val NoteColorPurple = Color(0xFF6C4CF1)
val NoteColorBlue = Color(0xFF3672E0)
val NoteColorTeal = Color(0xFF0EA394)
val NoteColorEmerald = Color(0xFF23A26D)
val NoteColorAmber = Color(0xFFE09A16)
val NoteColorRose = Color(0xFFE44A73)
val NoteColorIndigo = Color(0xFF5350D6)
val NoteColorSlate = Color(0xFF6B7482)

// ---------------------------------------------------------------------------------------------
// Category pastel tints (card backgrounds)
// ---------------------------------------------------------------------------------------------

val TintPurple = Color(0xFFEFEAFF)
val TintBlue = Color(0xFFE7F0FF)
val TintTeal = Color(0xFFDFF6F3)
val TintEmerald = Color(0xFFE2F7EB)
val TintAmber = Color(0xFFFDF1DA)
val TintRose = Color(0xFFFFE8EF)
val TintIndigo = Color(0xFFE9E9FF)
val TintSlate = Color(0xFFEFF1F5)

// Slightly deeper tint used for the card footer rail on light theme.
val TintAmberDeep = Color(0xFFF8DEA9)
val TintPurpleDeep = Color(0xFFDCD2FF)
val TintBlueDeep = Color(0xFFCFE1FF)
val TintEmeraldDeep = Color(0xFFC6EFD9)
val TintRoseDeep = Color(0xFFFFCEDA)
val TintTealDeep = Color(0xFFBFEDE7)
val TintIndigoDeep = Color(0xFFD2D2FF)
val TintSlateDeep = Color(0xFFDDE1E8)
