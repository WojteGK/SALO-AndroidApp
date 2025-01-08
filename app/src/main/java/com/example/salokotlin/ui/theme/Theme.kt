package com.example.salokotlin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Define your Shapes
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun SaloKotlinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = DarkPrimary,
            primaryVariant = DarkPrimaryVariant,
            secondary = DarkSecondary,
            background = DarkBackground,
            surface = DarkSurface,
            onPrimary = DarkOnPrimary,
            onSecondary = DarkOnSecondary
        )
    } else {
        lightColors(
            primary = LightPrimary,
            primaryVariant = LightPrimaryVariant,
            secondary = LightSecondary,
            background = LightBackground,
            surface = LightSurface,
            onPrimary = LightOnPrimary,
            onSecondary = LightOnSecondary
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
