package com.learningblueprint.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SaffronYellow,
    onPrimary = DeepGreenDark,
    background = DeepGreenDark,
    surface = DeepGreenSurface,
    onBackground = PaperLight,
    onSurface = PaperLight
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGreenPrimary,
    onPrimary = Color.White,
    secondary = SaffronYellow,
    onSecondary = DeepGreenDark,
    background = PaperLight,
    surface = CardWhite,
    onBackground = InkDark,
    onSurface = InkDark
)

@Composable
fun LearningBlueprintTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
