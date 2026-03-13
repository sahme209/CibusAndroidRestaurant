package com.cibus.restaurant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RestaurantDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFA726),
    secondary = Color(0xFFFFB74D),
    tertiary = Color(0xFFFFCC80)
)

private val RestaurantLightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFFF57C00),
    tertiary = Color(0xFFFF9800)
)

@Composable
fun CibusRestaurantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RestaurantDarkColorScheme else RestaurantLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
