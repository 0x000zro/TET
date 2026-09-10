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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.feature.practice.components.PracticeSubmitButton
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.feature.question.components.QuestionErrorView
import com.example.ui.feature.question.components.QuestionLoadingView
import com.example.ui.feature.question.components.QuestionNotFoundView
import com.example.ui.theme.LocalDimensions

/**
 * Screen for single-question practice (Step 9).
 *
 * Flow:
 * Question -> Select one option -> Submit Answer -> Correct/Incorrect feedback -> Show correct answer -> Show explanation.
 *
 * Enforces:
 * - Single-choice selection (replacing previous selection).
 * - Frozen/immutable state post-submission.
 * - Clear accessible feedback not relying on color alone.
 * - In-memory state without persisting attempts yet.
 */
@Composable
fun QuestionPracticeView(
    questionId: String,
    questionIndex: Int,
    viewModel: PracticeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimensions = LocalDimensions.current

    LaunchedEffect(questionId, questionIndex) {
        viewModel.loadQuestion(questionId, questionIndex)
    }

    when (val state = uiState) {
        is PracticeUiState.Loading -> {
            QuestionLoadingView(modifier = modifier)
        }
        is PracticeUiState.NotFound -> {
            QuestionNotFoundView(modifier = modifier)
        }
        is PracticeUiState.Error -> {
            QuestionErrorView(
                message = state.message,
                onRetry = { viewModel.retry() },
                modifier = modifier
            )
        }
        is PracticeUiState.Ready -> {
            val question = state.question
            val scrollState = rememberScrollState()

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("practice_screen"),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(dimensions.cornerSmall),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.question_number_label, question.questionNumber),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = dimensions.spacingSmall,
                                            vertical = dimensions.spacingExtraSmall
                                        )
                                    )
                                }

                                DifficultyChip(difficulty = question.difficulty)
                            }

                            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    // Prompt text
                    Text(
                        text = stringResource(R.string.practice_select_prompt),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Options List (Interactive)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        question.options.forEach { option ->
                            PracticeOptionCard(
                                option = option,
                                isSelected = option.id == state.selectedOptionId,
                                isSubmitted = false,
                                isCorrectOption = false,
                                isStudentSelection = false,
                                onSelect = { viewModel.selectOption(option.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                    // Submit Answer Button
                    PracticeSubmitButton(
                        canSubmit = state.canSubmit,
                        isSubmitted = false,
                        onSubmit = { viewModel.submitAnswer() }
                    )

                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))
                }
            }
        }
        is PracticeUiState.Submitted -> {
            val question = state.question
            val result = state.result
            val scrollState = rememberScrollState()

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("practice_screen"),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(dimensions.cornerSmall),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = stringResource(R.string.question_number_label, question.questionNumber),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = dimensions.spacingSmall,
                                            vertical = dimensions.spacingExtraSmall
                                        )
                                    )
                                }

                                DifficultyChip(difficulty = question.difficulty)
                            }

                            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    // Post-submission Feedback Banner
                    PracticeFeedbackBanner(result = result)

                    // Options List (Frozen with feedback)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        question.options.forEach { option ->
                            PracticeOptionCard(
                                option = option,
                                isSelected = option.id == state.selectedOptionId,
                                isSubmitted = true,
                                isCorrectOption = option.id == result.correctOptionId,
                                isStudentSelection = option.id == state.selectedOptionId,
                                onSelect = {}
                            )
                        }
                    }

                    // Explanation Card (Only if explanation is available)
                    if (result.explanation.isNotBlank()) {
                        PracticeExplanationCard(explanation = result.explanation)
                    }

                    // Submitted Button State
                    PracticeSubmitButton(
                        canSubmit = false,
                        isSubmitted = true,
                        onSubmit = {}
                    )

                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))
                }
            }
        }
    }
}
