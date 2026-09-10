package com.example.ui.feature.practice.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.practice.PracticeAttempt
import com.example.ui.theme.LocalDimensions
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Clean, focused component displaying recent local practice attempts (Step 11).
 *
 * Displays:
 * - Formatted date/time
 * - Percentage score
 * - Correct/Incorrect count
 * - Total questions count
 * - Accessible empty state when no attempts exist.
 */
@Composable
fun PracticeAttemptHistorySection(
    attempts: List<PracticeAttempt>,
    modifier: Modifier = Modifier,
    maxDisplayCount: Int = 5
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_history_section"),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingSmall))
            Text(
                text = stringResource(R.string.practice_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (attempts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_history_empty"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.practice_history_empty_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                    Text(
                        text = stringResource(R.string.practice_history_empty_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            val displayList = attempts.take(maxDisplayCount)
            displayList.forEach { attempt ->
                PracticeAttemptItemCard(attempt = attempt)
            }
        }
    }
}

@Composable
fun PracticeAttemptItemCard(
    attempt: PracticeAttempt,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    val formattedDate = remember(attempt.completedAt) {
        try {
            val date = Date(attempt.completedAt)
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()).format(date)
        } catch (_: Exception) {
            "Completed Practice"
        }
    }

    val formattedPercentage = remember(attempt.percentageScore) {
        String.format(Locale.getDefault(), "%.1f%%", attempt.percentageScore)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_attempt_item_${attempt.id}"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingExtraSmall)
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.practice_history_accuracy_details,
                        attempt.correctAnswers,
                        attempt.totalQuestions,
                        attempt.incorrectAnswers
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(dimensions.cornerPill),
                color = if (attempt.percentageScore >= 60.0) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                },
                modifier = Modifier.testTag("practice_attempt_score_${attempt.id}")
            ) {
                Text(
                    text = formattedPercentage,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (attempt.percentageScore >= 60.0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingExtraSmall)
                )
            }
        }
    }
}
