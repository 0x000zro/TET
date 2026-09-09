package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized dimensions and spacing system following Material Design 3 guidelines.
 */
data class Dimensions(
    val spacingNone: Dp = 0.dp,
    val spacingExtraSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 16.dp,
    val spacingLarge: Dp = 24.dp,
    val spacingExtraLarge: Dp = 32.dp,
    val spacingHuge: Dp = 48.dp,

    val cornerExtraSmall: Dp = 4.dp,
    val cornerSmall: Dp = 8.dp,
    val cornerMedium: Dp = 16.dp,
    val cornerLarge: Dp = 24.dp,
    val cornerPill: Dp = 999.dp,

    val iconSmall: Dp = 18.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconHero: Dp = 56.dp,

    val minTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 48.dp,
    val chipHeight: Dp = 36.dp,

    val elevationNone: Dp = 0.dp,
    val elevationLow: Dp = 2.dp,
    val elevationMedium: Dp = 4.dp,
    val elevationHigh: Dp = 8.dp,

    val maxContentWidth: Dp = 640.dp,
    val maxCardWidth: Dp = 600.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }
