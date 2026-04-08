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
    surface = CibusSurface,
    onPrimary = Color.White,
    onBackground = CibusTextPrimary,
    onSurface = CibusTextPrimary,
    onSurfaceVariant = CibusTextSecondary,
)

private val RestaurantLightColorScheme = lightColorScheme(
    primary = CibusGreen,
    secondary = CibusGreenLight,
    tertiary = CibusAmber,
    background = CibusSurfaceSecondary,
    surface = CibusCardBg,
    onPrimary = Color.White,
    onBackground = CibusTextOnSurface,
    onSurface = CibusTextOnSurface,
    onSurfaceVariant = CibusTextOnSurfaceSecondary,
)

@Composable
fun CibusRestaurantTheme(
    darkTheme: Boolean = false, // Always light mode to match iOS
    content: @Composable () -> Unit
) {
    val colorScheme = RestaurantLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
