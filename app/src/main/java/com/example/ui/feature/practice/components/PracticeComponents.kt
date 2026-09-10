package com.example.ui.feature.practice.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.practice.PracticeAnswerResult
import com.example.domain.model.practice.PracticeEvaluationStatus
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.SuccessGreen

/**
 * Visual feedback banner shown after an answer is submitted.
 * Uses both text and icons in addition to color to ensure full accessibility.
 */
@Composable
fun PracticeFeedbackBanner(
    result: PracticeAnswerResult,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val isCorrect = result.status == PracticeEvaluationStatus.CORRECT

    val containerColor = if (isCorrect) SuccessGreen.copy(alpha = 0.14f) else ErrorRed.copy(alpha = 0.12f)
    val borderColor = if (isCorrect) SuccessGreen else ErrorRed
    val contentColor = if (isCorrect) SuccessGreen else ErrorRed
    val icon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel
    val title = if (isCorrect) {
        stringResource(R.string.practice_result_correct_title)
    } else {
        stringResource(R.string.practice_result_incorrect_title)
    }
    val message = if (isCorrect) {
        stringResource(R.string.practice_result_correct_message)
    } else {
        stringResource(R.string.practice_result_incorrect_message)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_feedback_banner")
            .semantics {
                contentDescription = "$title: $message"
            },
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Displays the explanation for the solution after an answer is submitted.
 * Strictly omitted if the explanation is empty or blank.
 */
@Composable
fun PracticeExplanationCard(
    explanation: String,
    modifier: Modifier = Modifier
) {
    if (explanation.isBlank()) return

    val dimensions = LocalDimensions.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_explanation_card"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.question_explanation_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Action button for submitting the selected answer.
 * Disabled until an option is selected.
 * Becomes inactive/frozen once submitted.
 */
@Composable
fun PracticeSubmitButton(
    canSubmit: Boolean,
    isSubmitted: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    if (!isSubmitted) {
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensions.buttonHeight)
                .testTag("practice_submit_button"),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.practice_submit_button),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = dimensions.buttonHeight)
                .testTag("practice_submit_button"),
            shape = RoundedCornerShape(dimensions.cornerMedium)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingSmall))
            Text(
                text = stringResource(R.string.practice_submitted_button),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
