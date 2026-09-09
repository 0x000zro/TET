package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalDimensions

/**
 * Reusable M3 Card with standardized corners, borders, elevations,
 * and optional click handling with accessible touch target bounds.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues? = null,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    val dimensions = LocalDimensions.current
    val cardShape = shape ?: RoundedCornerShape(dimensions.cornerMedium)
    val defaultPadding = contentPadding ?: PaddingValues(dimensions.spacingMedium)

    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor
    )

    val cardBorder = BorderStroke(1.dp, borderColor)

    val baseModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = baseModifier,
            shape = cardShape,
            colors = cardColors,
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.padding(defaultPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = baseModifier,
            shape = cardShape,
            colors = cardColors,
            border = cardBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(modifier = Modifier.padding(defaultPadding)) {
                content()
            }
        }
    }
}
