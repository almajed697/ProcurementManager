package com.procurement.manager.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Palette ──────────────────────────────────────────────
val PrimaryBlue      = Color(0xFF1565C0)
val PrimaryDark      = Color(0xFF003C8F)
val SecondaryTeal    = Color(0xFF00897B)
val AccentAmber      = Color(0xFFFFB300)
val ErrorRed         = Color(0xFFD32F2F)
val SuccessGreen     = Color(0xFF388E3C)
val WarningOrange    = Color(0xFFF57C00)
val SurfaceLight     = Color(0xFFF5F7FA)
val OnSurfaceVariant = Color(0xFF546E7A)

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryBlue,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    secondary        = SecondaryTeal,
    onSecondary      = Color.White,
    tertiary         = AccentAmber,
    error            = ErrorRed,
    background       = SurfaceLight,
    surface          = Color.White,
    onBackground     = Color(0xFF1C1B1F),
    onSurface        = Color(0xFF1C1B1F),
    surfaceVariant   = Color(0xFFE8EEF4),
    onSurfaceVariant = OnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF90CAF9),
    onPrimary        = Color(0xFF003C8F),
    primaryContainer = Color(0xFF1565C0),
    secondary        = Color(0xFF80CBC4),
    tertiary         = AccentAmber,
    error            = Color(0xFFEF9A9A),
    background       = Color(0xFF121212),
    surface          = Color(0xFF1E1E1E),
)

@Composable
fun ProcurementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

val AppTypography = Typography(
    displayLarge  = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp),
    headlineLarge = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    headlineMedium= androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge    = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium   = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge     = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium    = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge    = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)
