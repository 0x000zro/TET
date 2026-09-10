package com.example.ui.feature.practice

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.feature.practice.components.PracticeExplanationCard
import com.example.ui.feature.practice.components.PracticeFeedbackBanner
import com.example.ui.feature.practice.components.PracticeOptionCard
import com.example.ui.feature.practice.components.PracticeSessionResultView
import com.example.ui.feature.practice.components.PracticeSubmitButton
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.feature.question.components.QuestionEmptyView
import com.example.ui.feature.question.components.QuestionErrorView
import com.example.ui.feature.question.components.QuestionLoadingView
import com.example.ui.theme.LocalDimensions

/**
 * Screen rendering a multi-question practice session (Step 10).
 *
 * Flow:
 * Start Practice -> Question 1 -> Option Selection -> Submit Answer -> Feedback + Answer + Explanation -> Next Question -> Repeat -> Finish -> Session Result.
 */
@Composable
fun PracticeSessionView(
    subtopicId: String,
    viewModel: PracticeSessionViewModel,
    onBackToSubtopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.sessionState.collectAsState()
    val dimensions = LocalDimensions.current

    LaunchedEffect(subtopicId) {
        viewModel.startSession(subtopicId)
    }

    when (val state = uiState) {
        is PracticeSessionUiState.Loading -> {
            QuestionLoadingView(modifier = modifier)
        }
        is PracticeSessionUiState.Empty -> {
            QuestionEmptyView(
                message = stringResource(R.string.practice_session_empty_message),
                modifier = modifier
            )
        }
        is PracticeSessionUiState.Error -> {
            QuestionErrorView(
                message = state.message,
                onRetry = { viewModel.retry() },
                modifier = modifier
            )
        }
        is PracticeSessionUiState.Completed -> {
            val attempts by viewModel.subtopicAttempts.collectAsState()
            PracticeSessionResultView(
                result = state.result,
                recentAttempts = attempts,
                onRestart = { viewModel.startSession(subtopicId) },
                onDone = onBackToSubtopic,
                modifier = modifier
            )
        }
        is PracticeSessionUiState.ActiveQuestion -> {
            val question = state.question
            val scrollState = rememberScrollState()

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("practice_session_screen"),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = dimensions.maxContentWidth)
                        .verticalScroll(scrollState)
                        .padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                ) {
                    // Progress Header: "Question X of Y" + Linear Progress Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("practice_session_progress_card"),
                        shape = RoundedCornerShape(dimensions.cornerMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spacingMedium)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.practice_session_progress,
                                        state.currentQuestionIndex,
                                        state.totalQuestions
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("practice_session_progress_text")
                                )

                                DifficultyChip(difficulty = question.difficulty)
                            }

                            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                            val progressFraction = if (state.totalQuestions > 0) {
                                state.currentQuestionIndex.toFloat() / state.totalQuestions.toFloat()
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .testTag("practice_session_progress_bar"),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }

                    // Question Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("practice_question_card"),
                        shape = RoundedCornerShape(dimensions.cornerMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.spacingMedium)
                        ) {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    // Post-submission feedback banner
                    if (state.isSubmitted && state.result != null) {
                        PracticeFeedbackBanner(result = state.result)
                    } else {
                        Text(
                            text = stringResource(R.string.practice_select_prompt),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Options List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        question.options.forEach { option ->
                            PracticeOptionCard(
                                option = option,
                                isSelected = option.id == state.selectedOptionId,
                                isSubmitted = state.isSubmitted,
                                isCorrectOption = state.isSubmitted && state.result != null && option.id == state.result.correctOptionId,
                                isStudentSelection = state.isSubmitted && option.id == state.selectedOptionId,
                                onSelect = { viewModel.selectOption(option.id) }
                            )
                        }
                    }

                    // Explanation Card post-submission
                    if (state.isSubmitted && state.result != null && state.result.explanation.isNotBlank()) {
                        PracticeExplanationCard(explanation = state.result.explanation)
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                    // Action Controls: Submit or Next / Finish
                    if (!state.isSubmitted) {
                        PracticeSubmitButton(
                            canSubmit = state.canSubmit,
                            isSubmitted = false,
                            onSubmit = { viewModel.submitAnswer() }
                        )
                    } else {
                        if (!state.isLastQuestion) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensions.buttonHeight)
                                    .testTag("practice_next_button"),
                                shape = RoundedCornerShape(dimensions.cornerMedium)
                            ) {
                                Text(
                                    text = stringResource(R.string.practice_next_question),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensions.iconSmall)
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.finishSession() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensions.buttonHeight)
                                    .testTag("practice_finish_button"),
                                shape = RoundedCornerShape(dimensions.cornerMedium)
                            ) {
                                Text(
                                    text = stringResource(R.string.practice_finish_session),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimensions.iconSmall)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))
                }
            }
        }
    }
}
