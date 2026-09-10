package com.example.ui.feature.wrongquestion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.wrongquestion.WrongQuestionSummary
import com.example.ui.feature.question.QuestionDetailView
import com.example.ui.feature.question.QuestionViewModel
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalDimensions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying the student's Wrong Questions / Mistake Review module (Step 13).
 *
 * Implements:
 * - Reactive listing of student mistakes with deterministic ordering (`lastWrongAt DESC`).
 * - Optional subtopic filter chips.
 * - Single source of truth question review reusing [QuestionDetailView].
 * - Correct answer and explanation displayed only in intentional review context.
 * - Explicit mistake removal action without affecting question or syllabus tables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongQuestionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WrongQuestionsViewModel = viewModel(),
    questionViewModel: QuestionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedQuestion by viewModel.selectedQuestion.collectAsState()
    val dimensions = LocalDimensions.current

    // Review mode: When a specific wrong question is opened
    if (selectedQuestion != null) {
        val summary = selectedQuestion!!
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("wrong_question_review_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.wrong_questions_review_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.clearSelectedQuestion() },
                            modifier = Modifier.testTag("wrong_question_review_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.wrong_questions_back_to_list)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            QuestionDetailView(
                questionId = summary.question.id,
                questionIndex = 1,
                viewModel = questionViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                reviewCorrectOptionId = summary.question.options.firstOrNull { it.isCorrect }?.id,
                reviewHeaderContent = {
                    ReviewMistakeHeaderCard(
                        summary = summary,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                reviewBottomContent = {
                    OutlinedButton(
                        onClick = { viewModel.removeWrongQuestion(summary.question.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = dimensions.buttonHeight)
                            .testTag("remove_wrong_question_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Text(
                            text = stringResource(R.string.wrong_questions_remove_action),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        }
        return
    }

    // List mode: Displaying all mistake records
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("wrong_questions_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.wrong_questions_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("wrong_questions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_home)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is WrongQuestionsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("wrong_questions_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                }

                is WrongQuestionsUiState.Empty -> {
                    WrongQuestionsEmptyView(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is WrongQuestionsUiState.Error -> {
                    WrongQuestionsErrorView(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is WrongQuestionsUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = dimensions.maxContentWidth)
                    ) {
                        // Subtopic Filter Chips (if multiple subtopics exist)
                        if (state.availableSubtopics.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(
                                        horizontal = dimensions.spacingMedium,
                                        vertical = dimensions.spacingSmall
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                            ) {
                                FilterChip(
                                    selected = state.selectedSubtopicId == null,
                                    onClick = { viewModel.filterBySubtopic(null) },
                                    label = { Text(stringResource(R.string.wrong_questions_all_subtopics)) },
                                    modifier = Modifier.testTag("filter_chip_all")
                                )
                                state.availableSubtopics.forEach { (id, name) ->
                                    FilterChip(
                                        selected = state.selectedSubtopicId == id,
                                        onClick = { viewModel.filterBySubtopic(id) },
                                        label = { Text(name) },
                                        modifier = Modifier.testTag("filter_chip_$id")
                                    )
                                }
                            }
                        }

                        // Mistakes LazyColumn
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("wrong_questions_list"),
                            contentPadding = PaddingValues(
                                start = dimensions.spacingMedium,
                                end = dimensions.spacingMedium,
                                top = dimensions.spacingSmall,
                                bottom = dimensions.spacingExtraLarge
                            ),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            items(
                                items = state.items,
                                key = { it.wrongQuestion.questionId }
                            ) { summary ->
                                WrongQuestionItemCard(
                                    summary = summary,
                                    onReviewClick = { viewModel.selectQuestionForReview(summary) },
                                    onRemoveClick = { viewModel.removeWrongQuestion(summary.wrongQuestion.questionId) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Mistake card in the Wrong Questions list.
 */
@Composable
fun WrongQuestionItemCard(
    summary: WrongQuestionSummary,
    onReviewClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val wrongQ = summary.wrongQuestion
    val question = summary.question

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .clickable(onClick = onReviewClick)
            .testTag("wrong_question_card_${wrongQ.questionId}"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.elevationLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            // Header Row: Subtopic Pill & Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerPill),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = summary.subtopicName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                DifficultyChip(difficulty = question.difficulty)
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Question Statement Snippet
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Mistake Metadata: Wrong Count & Last Wrong Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerPill),
                    color = ErrorRed.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                if (wrongQ.wrongCount <= 1) R.string.wrong_questions_wrong_count_singular
                                else R.string.wrong_questions_wrong_count_badge,
                                wrongQ.wrongCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTimestamp(wrongQ.lastWrongAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Action Buttons: Review & Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Button(
                    onClick = onReviewClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("review_wrong_question_${wrongQ.questionId}"),
                    shape = RoundedCornerShape(dimensions.cornerSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Text(
                        text = stringResource(R.string.wrong_questions_review_action),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.testTag("remove_wrong_question_${wrongQ.questionId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.wrong_questions_remove_action),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Header card displayed during intentional review of a wrong question.
 */
@Composable
fun ReviewMistakeHeaderCard(
    summary: WrongQuestionSummary,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val wrongQ = summary.wrongQuestion

    Card(
        modifier = modifier.testTag("review_mistake_header_card"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(dimensions.spacingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Text(
                        text = stringResource(
                            if (wrongQ.wrongCount <= 1) R.string.wrong_questions_wrong_count_singular
                            else R.string.wrong_questions_wrong_count_badge,
                            wrongQ.wrongCount
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }

                Text(
                    text = summary.subtopicName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = stringResource(R.string.wrong_questions_last_wrong_time, formatTimestamp(wrongQ.lastWrongAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state when no mistake records exist.
 */
@Composable
fun WrongQuestionsEmptyView(
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensions.spacingLarge)
            .testTag("wrong_questions_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(dimensions.cornerPill),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

        Text(
            text = stringResource(R.string.wrong_questions_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = stringResource(R.string.wrong_questions_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp)
        )
    }
}

/**
 * Error state when loading mistake records fails.
 */
@Composable
fun WrongQuestionsErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensions.spacingLarge)
            .testTag("wrong_questions_error_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

        Text(
            text = stringResource(R.string.wrong_questions_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

        Button(
            onClick = onRetry,
            modifier = Modifier.testTag("wrong_questions_retry_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingSmall))
            Text(text = stringResource(R.string.wrong_questions_retry))
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Recently"
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
