package com.example.ui.feature.question

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.domain.model.Subtopic
import com.example.ui.feature.practice.components.PracticeAttemptHistorySection
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.feature.question.components.QuestionEmptyView
import com.example.ui.feature.question.components.QuestionErrorView
import com.example.ui.feature.question.components.QuestionItemCard
import com.example.ui.feature.question.components.QuestionLoadingView
import com.example.ui.feature.question.components.QuestionNotFoundView
import com.example.ui.feature.question.components.QuestionOptionCard
import com.example.ui.feature.question.model.QuestionPresentationModel
import com.example.ui.theme.LocalDimensions

/**
 * Question List browsing view for an active Subtopic.
 */
@Composable
fun QuestionListView(
    subtopic: Subtopic,
    viewModel: QuestionViewModel,
    onQuestionClick: (QuestionPresentationModel) -> Unit,
    modifier: Modifier = Modifier,
    onStartPractice: (() -> Unit)? = null
) {
    val uiState by viewModel.listState.collectAsState()
    val dimensions = LocalDimensions.current

    LaunchedEffect(subtopic.id) {
        viewModel.loadQuestionsForSubtopic(subtopic.id)
    }

    when (val state = uiState) {
        is QuestionListUiState.Loading -> {
            QuestionLoadingView(modifier = modifier)
        }
        is QuestionListUiState.Empty -> {
            QuestionEmptyView(modifier = modifier)
        }
        is QuestionListUiState.Error -> {
            QuestionErrorView(
                message = state.message,
                onRetry = { viewModel.retryQuestions() },
                modifier = modifier
            )
        }
        is QuestionListUiState.Success -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .widthIn(max = dimensions.maxContentWidth)
                    .testTag("question_list"),
                contentPadding = PaddingValues(dimensions.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                if (onStartPractice != null && state.questions.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_practice_session_card"),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensions.spacingMedium),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.practice_session_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = stringResource(R.string.question_count_label, state.questions.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = onStartPractice,
                                    modifier = Modifier.testTag("start_practice_session_button"),
                                    shape = RoundedCornerShape(dimensions.cornerSmall)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        modifier = Modifier.size(dimensions.iconSmall)
                                    )
                                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                                    Text(
                                        text = stringResource(R.string.practice_start_subtopic_button),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    val attempts by viewModel.subtopicAttempts.collectAsState()
                    PracticeAttemptHistorySection(attempts = attempts)
                }

                items(
                    items = state.questions,
                    key = { it.id }
                ) { question ->
                    QuestionItemCard(
                        question = question,
                        onClick = { onQuestionClick(question) }
                    )
                }
            }
        }
    }
}

/**
 * Question Detail presentation view.
 * Displays question text, all options (strictly neutral without answer reveal by default),
 * and explanation when available.
 * In intentional review mode, highlights the correct option and supports mistake review actions.
 */
@Composable
fun QuestionDetailView(
    questionId: String,
    questionIndex: Int,
    viewModel: QuestionViewModel,
    modifier: Modifier = Modifier,
    onPracticeClick: (() -> Unit)? = null,
    reviewCorrectOptionId: String? = null,
    reviewHeaderContent: (@Composable () -> Unit)? = null,
    reviewBottomContent: (@Composable () -> Unit)? = null
) {
    val uiState by viewModel.detailState.collectAsState()
    val dimensions = LocalDimensions.current

    LaunchedEffect(questionId, questionIndex) {
        viewModel.loadQuestionDetail(questionId, questionIndex)
    }

    when (val state = uiState) {
        is QuestionDetailUiState.Loading -> {
            QuestionLoadingView(modifier = modifier)
        }
        is QuestionDetailUiState.NotFound -> {
            QuestionNotFoundView(modifier = modifier)
        }
        is QuestionDetailUiState.Error -> {
            QuestionErrorView(
                message = state.message,
                onRetry = { viewModel.retryQuestionDetail() },
                modifier = modifier
            )
        }
        is QuestionDetailUiState.Success -> {
            val question = state.question
            val scrollState = rememberScrollState()

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .widthIn(max = dimensions.maxContentWidth)
                    .verticalScroll(scrollState)
                    .padding(dimensions.spacingMedium)
                    .testTag("question_detail_content")
            ) {
                // Header: Question index badge & Difficulty badge & Bookmark toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(dimensions.cornerPill),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.question_number_label, question.questionNumber),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                    ) {
                        DifficultyChip(difficulty = question.difficulty)

                        val isBookmarked by viewModel.isBookmarked.collectAsState()
                        IconButton(
                            onClick = { viewModel.toggleBookmark(question.id, question.subtopicId) },
                            modifier = Modifier.testTag(if (isBookmarked) "bookmark_button_active" else "bookmark_button_inactive")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isBookmarked) stringResource(R.string.bookmark_remove_action) else stringResource(R.string.bookmark_add_action),
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (reviewHeaderContent != null) {
                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))
                    reviewHeaderContent()
                }

                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                // Question Statement Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_text_card"),
                    shape = RoundedCornerShape(dimensions.cornerMedium),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensions.elevationLow)
                ) {
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(dimensions.spacingMedium)
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.spacingLarge))

                // Options Section Header
                Text(
                    text = stringResource(R.string.question_options_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                // Deterministically ordered options (Neutral presentation without answer leak by default; highlights correct answer in review mode)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    question.options.forEach { option ->
                        QuestionOptionCard(
                            option = option,
                            isCorrectAnswer = reviewCorrectOptionId != null && option.id == reviewCorrectOptionId
                        )
                    }
                }

                // Explanation Section (when available in stored data)
                if (question.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("question_explanation"),
                        shape = RoundedCornerShape(dimensions.cornerMedium),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(dimensions.spacingMedium)
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
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Practice Action Button (Step 9)
                if (onPracticeClick != null) {
                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))
                    Button(
                        onClick = onPracticeClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = dimensions.buttonHeight)
                            .testTag("practice_question_button"),
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Text(
                            text = stringResource(R.string.practice_this_question_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Intentional Review Bottom Actions (e.g. Remove Mistake)
                if (reviewBottomContent != null) {
                    Spacer(modifier = Modifier.height(dimensions.spacingLarge))
                    reviewBottomContent()
                }

                Spacer(modifier = Modifier.height(dimensions.spacingExtraLarge))
            }
        }
    }
}
