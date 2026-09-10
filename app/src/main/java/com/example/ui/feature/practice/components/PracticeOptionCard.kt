package com.example.ui.feature.practice.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.feature.question.model.QuestionOptionPresentationModel
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.SuccessGreen

/**
 * Option card for the interactive single-question practice screen.
 *
 * Pre-submission:
 * - Neutral option styling with minimum 48dp touch target.
 * - Selecting replaces any previous selection.
 * - Does NOT indicate or leak correctness.
 *
 * Post-submission:
 * - Frozen (interaction disabled).
 * - Distinguishes student selection (correct/incorrect) and reveals the correct option.
 * - Accessible with clear textual badges and icons, not relying on color alone.
 */
@Composable
fun PracticeOptionCard(
    option: QuestionOptionPresentationModel,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrectOption: Boolean,
    isStudentSelection: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    val containerColor: Color
    val borderColor: Color
    val badgeLabel: String?
    val badgeIcon: androidx.compose.ui.graphics.vector.ImageVector?
    val badgeColor: Color

    if (!isSubmitted) {
        if (isSelected) {
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            borderColor = MaterialTheme.colorScheme.primary
        } else {
            containerColor = MaterialTheme.colorScheme.surface
            borderColor = MaterialTheme.colorScheme.outlineVariant
        }
        badgeLabel = null
        badgeIcon = null
        badgeColor = MaterialTheme.colorScheme.primary
    } else {
        when {
            isStudentSelection && isCorrectOption -> {
                containerColor = SuccessGreen.copy(alpha = 0.14f)
                borderColor = SuccessGreen
                badgeLabel = stringResource(R.string.practice_your_answer_correct)
                badgeIcon = Icons.Default.CheckCircle
                badgeColor = SuccessGreen
            }
            isStudentSelection && !isCorrectOption -> {
                containerColor = ErrorRed.copy(alpha = 0.12f)
                borderColor = ErrorRed
                badgeLabel = stringResource(R.string.practice_your_answer_incorrect)
                badgeIcon = Icons.Default.Cancel
                badgeColor = ErrorRed
            }
            !isStudentSelection && isCorrectOption -> {
                containerColor = SuccessGreen.copy(alpha = 0.10f)
                borderColor = SuccessGreen
                badgeLabel = stringResource(R.string.practice_correct_answer_badge)
                badgeIcon = Icons.Default.CheckCircle
                badgeColor = SuccessGreen
            }
            else -> {
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                badgeLabel = null
                badgeIcon = null
                badgeColor = MaterialTheme.colorScheme.onSurfaceVariant
            }
        }
    }

    val accessibilityDescription = buildString {
        append("Option ${option.label}: ${option.optionText}. ")
        if (!isSubmitted) {
            if (isSelected) append("Selected.") else append("Not selected.")
        } else {
            badgeLabel?.let { append("$it.") }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minTouchTarget)
            .testTag("practice_option_${option.label}")
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = accessibilityDescription
            }
            .then(
                if (!isSubmitted) {
                    Modifier.clickable(
                        role = Role.RadioButton,
                        onClick = onSelect
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected || (isSubmitted && isCorrectOption)) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Option Label Badge
                Surface(
                    shape = CircleShape,
                    color = if (isSubmitted && (isCorrectOption || isStudentSelection)) {
                        badgeColor.copy(alpha = 0.2f)
                    } else if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubmitted && (isCorrectOption || isStudentSelection)) {
                                badgeColor
                            } else if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(dimensions.spacingMedium))

                // Option Text
                Text(
                    text = option.optionText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSubmitted && !isCorrectOption && !isStudentSelection) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(dimensions.spacingSmall))

                // Trailing Radio Button or Status Indicator
                if (!isSubmitted) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null, // handled by outer card click
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                } else if (badgeIcon != null) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            // Post-submission text badge tag (ensures not relying on color alone)
            if (badgeLabel != null) {
                Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerPill),
                    color = badgeColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        badgeIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}
