package com.example.ui.feature.practice.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.model.practice.PracticeSessionResult
import com.example.ui.theme.LocalDimensions
import java.util.Locale

/**
 * Result summary screen displayed upon finishing a multi-question practice session (Step 10 & 11).
 *
 * Displays:
 * - Total questions
 * - Answered questions
 * - Correct answers
 * - Incorrect answers
 * - Score percentage
 * - Recent practice attempts history
 */
@Composable
fun PracticeSessionResultView(
    result: PracticeSessionResult,
    onRestart: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    recentAttempts: List<PracticeAttempt> = emptyList()
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("practice_session_result"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = dimensions.maxContentWidth)
                .verticalScroll(scrollState)
                .padding(dimensions.spacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Trophy / Completion Icon
            Surface(
                shape = RoundedCornerShape(dimensions.cornerPill),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Title
            Text(
                text = stringResource(R.string.practice_session_completed_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Primary Score Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_result_score_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.practice_session_score_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))

                    val formattedPercentage = String.format(Locale.getDefault(), "%.1f%%", result.scorePercentage)
                    Text(
                        text = formattedPercentage,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("practice_result_score")
                    )

                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                    // 4-Stat Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = stringResource(R.string.practice_session_total_questions),
                            value = result.totalQuestions.toString(),
                            tag = "practice_result_total"
                        )
                        StatItem(
                            label = stringResource(R.string.practice_session_answered_questions),
                            value = result.answeredQuestions.toString(),
                            tag = "practice_result_answered"
                        )
                        StatItem(
                            label = stringResource(R.string.practice_session_correct_answers),
                            value = result.correctAnswers.toString(),
                            valueColor = MaterialTheme.colorScheme.primary,
                            tag = "practice_result_correct"
                        )
                        StatItem(
                            label = stringResource(R.string.practice_session_incorrect_answers),
                            value = result.incorrectAnswers.toString(),
                            valueColor = MaterialTheme.colorScheme.error,
                            tag = "practice_result_incorrect"
                        )
                    }
                }
            }

            if (recentAttempts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                PracticeAttemptHistorySection(
                    attempts = recentAttempts,
                    maxDisplayCount = 3
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Action Buttons
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
                    .testTag("practice_restart_button"),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.practice_session_restart_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.buttonHeight)
                    .testTag("practice_done_button"),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.practice_session_done_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    tag: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    val dimensions = LocalDimensions.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = dimensions.spacingExtraSmall)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            modifier = Modifier.testTag(tag)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
