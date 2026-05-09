package com.cibus.restaurant.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand (unified with Customer app — primary is Starbucks green #00704A) ──
val CibusGreen = Color(0xFF00704A)       // primary (unified)
val CibusGreenDeep = Color(0xFF2D6A4F)   // deep forest accent
val CibusGreenDark = Color(0xFF1B4332)   // primaryDark
val CibusGreenLight = Color(0xFF40916C)  // primaryLight
val CibusAccent = Color(0xFF30D158)      // accent (Apple mint green)
val CibusAccentGreen = Color(0xFF00704A) // alias — backward compat

// ── Status ───────────────────────────────────────────────────────────────────
val CibusRed = Color(0xFFDC2626)         // danger
val CibusRedHot = Color(0xFFE53935)
val CibusAmber = Color(0xFFB45309)       // warning
val CibusAmberLight = Color(0xFFF59E0B)  // warningLight
val CibusOrange = Color(0xFFEA580C)      // orangeAlert
val CibusOrangeWarm = Color(0xFFF57C00)
val CibusCoral = Color(0xFFE07A5F)
val CibusSuccess = Color(0xFF16A34A)     // success

// ── Surfaces ─────────────────────────────────────────────────────────────────
val CibusCardBg = Color.White
val CibusCardBG = Color.White                   // alias (Customer app compat)
val CibusCardBGDark = Color(0xFF2C2C2E)         // dark mode card surface
val CibusSurfaceSecondary = Color(0xFFF2F2F7)   // iOS secondarySystemBackground
val CibusSurfaceGreen = Color(0xFFF0F7F4)

// ── Text ─────────────────────────────────────────────────────────────────────
/** Dark-theme shell text: light on dark. */
val CibusTextPrimary = Color(0xFFF5F5F7)
val CibusTextSecondary = Color(0xFFAEAEB2)
/** Light-theme text: dark on white (used in card-based views). */
val CibusTextOnSurface = Color(0xFF1C1C1E)
val CibusTextOnSurfaceSecondary = Color(0xFF8E8E93)
val CibusTextTertiary = Color(0xFFC7C7CC)

// ── Card/Header ──────────────────────────────────────────────────────────────
val CibusHeaderCard = Color(0xFF2C2C2E)
val CibusSurface = Color(0xFF2C2C2E)
val CibusSurfaceNeutral = Color(0xFF1D1D1F)
val CibusYellow = Color(0xFFFFC107)

// ── Accent / Icon tints ─────────────────────────────────────────────────────
val RestEmeraldStart = Color(0xFF009963)
val RestEmeraldMid = Color(0xFF00704A)
val RestEmeraldEnd = Color(0xFF004A32)
val RestAccentTeal = Color(0xFF4ADEAF)
val RestIconBlue = Color(0xFF007AFF)
val RestIconGreen = Color(0xFF34C759)
val RestIconOrange = Color(0xFFFF9F0A)
val RestIconPurple = Color(0xFFAF52DE)
val RestIconPink = Color(0xFFFF375F)
val RestIconTeal = Color(0xFF5AC8FA)
val RestLiveBadge = Color(0xFF34C759)

// ── Apple System Colors ─────────────────────────────────────────────────────
val AppleSystemGreen = Color(0xFF34C759)
val AppleSystemBlue = Color(0xFF007AFF)
val AppleSystemIndigo = Color(0xFF5856D6)
val AppleSystemTeal = Color(0xFF5AC8FA)
val AppleSystemOrange = Color(0xFFFF9F0A)
val AppleSystemRed = Color(0xFFFF3B30)
val AppleSystemPink = Color(0xFFFF375F)

// ── Refined neutral palette ─────────────────────────────────────────────────
val AppleWarmGray = Color(0xFFF5F5F7)      // maps to iOS systemGroupedBackground
val AppleCoolGray = Color(0xFFE5E5EA)
val AppleLabelPrimary = Color(0xFF1C1C1E)
val AppleLabelSecondary = Color(0xFF8E8E93)
val AppleLabelTertiary = Color(0xFFC7C7CC)
val AppleGroupedBackground = Color(0xFFF2F2F7)
val AppleElevatedSurface = Color(0xFFFFFFFF)
val AppleSeparator = Color(0xFFC6C6C8)

// ── Login tints ─────────────────────────────────────────────────────────────
val LoginGradientDeep = Color(0xFF0D2818)
val LoginGradientMid = Color(0xFF1B4332)
val LoginGradientBrand = Color(0xFF00704A)
val LoginGradientLight = Color(0xFF2D6A4F)
