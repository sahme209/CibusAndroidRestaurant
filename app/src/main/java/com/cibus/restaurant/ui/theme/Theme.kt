package com.cibus.restaurant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RestaurantDarkColorScheme = darkColorScheme(
    primary = CibusGreenLight,
    secondary = CibusAccent,
    tertiary = CibusAmberLight,
    background = CibusSurfaceNeutral,
    surface = Color(0xFF1C231F),           // green-tinted card surface
    onPrimary = Color.White,
    onBackground = CibusTextPrimary,
    onSurface = CibusTextPrimary,
    onSurfaceVariant = CibusTextSecondary,
    outline = Color(0xFF38383A),             // Apple dark separator
)

private val RestaurantLightColorScheme = lightColorScheme(
    primary = CibusGreen,                    // unified #00704A
    secondary = CibusGreenLight,
    tertiary = CibusAmber,
    background = AppleGroupedBackground,      // Apple grouped table background (#F2F2F7)
    surface = AppleElevatedSurface,          // pure white
    onPrimary = Color.White,
    onBackground = CibusTextOnSurface,
    onSurface = CibusTextOnSurface,
    onSurfaceVariant = CibusTextOnSurfaceSecondary,
    surfaceVariant = AppleWarmGray,          // #F5F5F7
    outline = AppleSeparator,               // #C6C6C8
)

@Composable
fun CibusRestaurantTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RestaurantDarkColorScheme else RestaurantLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
