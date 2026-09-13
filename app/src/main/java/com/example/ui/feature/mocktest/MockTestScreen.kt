package com.example.ui.feature.mocktest

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestPerformanceRating
import com.example.domain.model.mocktest.MockTestQuestionOutcomeStatus
import com.example.domain.model.mocktest.MockTestResult
import com.example.ui.feature.mocktest.model.MockTestPaletteItem
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.feature.question.components.QuestionEmptyView
import com.example.ui.feature.question.components.QuestionErrorView
import com.example.ui.feature.question.components.QuestionLoadingView
import com.example.ui.feature.question.model.QuestionOptionPresentationModel
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.SuccessGreen
import java.util.Locale

/**
 * Main Composable screen for Mock Test execution (Step 17).
 *
 * Flow:
 * Configuration Overview -> Start Test -> Active Question Screen -> Finish Confirmation Dialog -> Result Summary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MockTestViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimensions = LocalDimensions.current

    val isActiveTest = uiState is MockTestUiState.ActiveTest

    // Intercept system Back when active test is in progress
    BackHandler(enabled = isActiveTest) {
        viewModel.setAbandonConfirmationVisible(true)
    }

    val handleBackClick = {
        if (isActiveTest) {
            viewModel.setAbandonConfirmationVisible(true)
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value is MockTestUiState.Loading) {
            viewModel.loadTestConfiguration()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.mock_test_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBackClick,
                        modifier = Modifier.testTag("mock_test_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_navigation)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is MockTestUiState.Loading -> {
                    QuestionLoadingView(modifier = Modifier.fillMaxSize())
                }
                is MockTestUiState.Empty -> {
                    QuestionEmptyView(
                        message = state.message,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is MockTestUiState.Error -> {
                    QuestionErrorView(
                        message = state.message,
                        onRetry = { viewModel.restartTest() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is MockTestUiState.ConfigurationOverview -> {
                    MockTestOverviewView(
                        state = state,
                        onStartTest = { viewModel.startTest() }
                    )
                }
                is MockTestUiState.ActiveTest -> {
                    MockTestActiveView(
                        state = state,
                        onSelectOption = { optionId -> viewModel.selectAnswer(optionId) },
                        onClearAnswer = { viewModel.clearAnswer() },
                        onPrevious = { viewModel.previousQuestion() },
                        onNext = { viewModel.nextQuestion() },
                        onNavigateToQuestion = { index -> viewModel.navigateToQuestion(index) },
                        onRequestFinish = { viewModel.setFinishConfirmationVisible(true) },
                        onConfirmFinish = { viewModel.confirmFinish() },
                        onDismissFinish = { viewModel.setFinishConfirmationVisible(false) },
                        onConfirmAbandon = {
                            viewModel.setAbandonConfirmationVisible(false)
                            viewModel.abandonTest()
                            onNavigateBack()
                        },
                        onDismissAbandon = { viewModel.setAbandonConfirmationVisible(false) }
                    )
                }
                is MockTestUiState.ResultSummary -> {
                    MockTestResultView(
                        result = state.result,
                        configuration = state.configuration,
                        timeUsedSeconds = state.timeUsedSeconds,
                        timeRemainingSeconds = state.timeRemainingSeconds,
                        onRestart = { viewModel.restartTest() },
                        onBackToOverview = { viewModel.returnToOverview() },
                        onDone = onNavigateBack
                    )
                }
            }
        }
    }
}

/**
 * Overview screen introducing the mock test, showing scope, duration, and instructions.
 */
@Composable
private fun MockTestOverviewView(
    state: MockTestUiState.ConfigurationOverview,
    onStartTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_overview_view"),
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
            // Header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_header_card"),
                shape = RoundedCornerShape(dimensions.cornerLarge),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingLarge)
                ) {
                    Text(
                        text = state.configuration.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.mock_test_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Test parameters summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_params_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OverviewParamRow(
                        label = stringResource(R.string.mock_test_exam_label),
                        value = state.examTitle
                    )
                    OverviewParamRow(
                        label = stringResource(R.string.mock_test_paper_label),
                        value = state.paperTitle
                    )
                    OverviewParamRow(
                        label = stringResource(R.string.mock_test_scope_label),
                        value = state.scopeDescription
                    )
                    OverviewParamRow(
                        label = stringResource(R.string.mock_test_questions_label),
                        value = "${state.configuration.questionCount} Questions"
                    )
                    OverviewParamRow(
                        label = stringResource(R.string.mock_test_duration_label),
                        value = stringResource(R.string.mock_test_duration_value, state.configuration.durationMinutes)
                    )
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_instructions_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Text(
                            text = stringResource(R.string.mock_test_instructions_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = stringResource(R.string.mock_test_instruction_1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.mock_test_instruction_2),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.mock_test_instruction_3),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Start Test CTA
            Button(
                onClick = onStartTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = dimensions.minTouchTarget)
                    .testTag("start_mock_test_button"),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.mock_test_start_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OverviewParamRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Screen rendering an active question in the mock test.
 */
@Composable
private fun MockTestActiveView(
    state: MockTestUiState.ActiveTest,
    onSelectOption: (String) -> Unit,
    onClearAnswer: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onNavigateToQuestion: (Int) -> Unit = {},
    onRequestFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
    onDismissFinish: () -> Unit,
    onConfirmAbandon: () -> Unit,
    onDismissAbandon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()
    val question = state.currentQuestion

    if (state.showAbandonConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissAbandon,
            title = {
                Text(
                    text = stringResource(R.string.mock_test_abandon_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.mock_test_abandon_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmAbandon,
                    modifier = Modifier.testTag("confirm_abandon_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.mock_test_abandon_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissAbandon,
                    modifier = Modifier.testTag("dismiss_abandon_button")
                ) {
                    Text(stringResource(R.string.mock_test_abandon_dialog_dismiss))
                }
            }
        )
    }

    if (state.showFinishConfirmation) {
        val hasUnanswered = state.unansweredCount > 0
        AlertDialog(
            onDismissRequest = onDismissFinish,
            modifier = Modifier.testTag("mock_test_finish_dialog"),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasUnanswered) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            if (hasUnanswered) {
                                R.string.mock_test_finish_dialog_unanswered_title
                            } else {
                                R.string.mock_test_finish_dialog_title
                            }
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (hasUnanswered) {
                        stringResource(
                            R.string.mock_test_finish_dialog_unanswered_message,
                            state.answeredCount,
                            state.totalQuestions,
                            state.unansweredCount
                        )
                    } else {
                        stringResource(
                            R.string.mock_test_finish_dialog_all_answered_message,
                            state.totalQuestions
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmFinish,
                    modifier = Modifier.testTag("confirm_finish_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.mock_test_action_finish_test))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissFinish,
                    modifier = Modifier.testTag("dismiss_finish_button")
                ) {
                    Text(stringResource(R.string.mock_test_action_continue_review))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_active_view"),
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
            // Progress Bar & Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_progress_header"),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.mock_test_progress_format,
                            state.currentQuestionIndex,
                            state.totalQuestions
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Countdown Timer Badge
                        val isWarning = state.remainingSeconds in 1..60
                        val isExpired = state.remainingSeconds <= 0
                        val timerContainerColor = when {
                            isExpired || isWarning -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                        val timerContentColor = when {
                            isExpired || isWarning -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }

                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerPill),
                            color = timerContainerColor,
                            modifier = Modifier.testTag("mock_test_timer_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = stringResource(R.string.mock_test_timer_description),
                                    tint = timerContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.formattedRemainingTime,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = timerContentColor,
                                    modifier = Modifier.testTag("mock_test_timer_text")
                                )
                            }
                        }

                        // Answered Status Badge
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerPill),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.mock_test_answered_status,
                                    state.answeredCount,
                                    state.unansweredCount
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val progress = if (state.totalQuestions > 0) {
                    state.currentQuestionIndex.toFloat() / state.totalQuestions.toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(dimensions.cornerPill))
                )
            }

            // Compact Status Summary (Step 20)
            MockTestStatusSummary(
                totalQuestions = state.totalQuestions,
                answeredCount = state.answeredCount,
                unansweredCount = state.unansweredCount,
                modifier = Modifier.fillMaxWidth()
            )

            // Question Palette (Step 19)
            MockTestQuestionPalette(
                paletteItems = state.paletteItems,
                onSelectQuestion = onNavigateToQuestion,
                modifier = Modifier.fillMaxWidth()
            )

            // Question Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_question_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerPill),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Q${state.currentQuestionIndex}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        DifficultyChip(difficulty = question.difficulty)
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_options_container"),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                question.options.forEach { option ->
                    val isSelected = option.id == state.selectedOptionId
                    MockTestOptionCard(
                        option = option,
                        isSelected = isSelected,
                        onSelect = { onSelectOption(option.id) }
                    )
                }
            }

            // Clear answer button (if answered)
            if (state.selectedOptionId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onClearAnswer,
                        modifier = Modifier.testTag("clear_answer_button")
                    ) {
                        Text(
                            text = stringResource(R.string.mock_test_action_clear),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Navigation Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_navigation_bar"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = state.hasPrevious,
                    modifier = Modifier
                        .defaultMinSize(minHeight = dimensions.minTouchTarget)
                        .testTag("mock_test_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSmall)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.mock_test_action_prev))
                }

                // Next or Finish button
                if (state.hasNext) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .defaultMinSize(minHeight = dimensions.minTouchTarget)
                            .testTag("mock_test_next_button")
                    ) {
                        Text(stringResource(R.string.mock_test_action_next))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                    }
                } else {
                    Button(
                        onClick = onRequestFinish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = dimensions.minTouchTarget)
                            .testTag("mock_test_finish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.mock_test_action_finish))
                    }
                }
            }
        }
    }
}

/**
 * Option Card for Mock Test answering.
 * Does not reveal correctness during testing.
 */
@Composable
private fun MockTestOptionCard(
    option: QuestionOptionPresentationModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = dimensions.minTouchTarget)
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .testTag("mock_test_option_${option.id}")
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "Option ${option.label}: ${option.optionText}. ${if (isSelected) "Selected." else "Not selected."}"
            },
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Text(
                text = option.optionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(dimensions.spacingSmall))

            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(dimensions.iconMedium)
            )
        }
    }
}

/**
 * Result Summary screen showing final performance score.
 */
/**
 * Result Summary screen showing final performance score, interpretation, timing, and question outcomes (Step 21).
 */
@Composable
private fun MockTestResultView(
    result: MockTestResult,
    configuration: MockTestConfiguration,
    timeUsedSeconds: Long,
    timeRemainingSeconds: Long,
    onRestart: () -> Unit,
    onBackToOverview: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_result_view"),
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
            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Trophy Icon
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.mock_test_result_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = configuration.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Primary Score Card with Deterministic Interpretation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_score_card"),
                shape = RoundedCornerShape(dimensions.cornerLarge),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_result_score_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = String.format(Locale.US, "%.1f%%", result.scorePercentage),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("mock_test_score_percentage")
                    )

                    // Performance Interpretation (Deterministic)
                    val ratingLabel = when (result.performanceRating) {
                        MockTestPerformanceRating.EXCELLENT -> stringResource(R.string.mock_test_rating_excellent)
                        MockTestPerformanceRating.GOOD -> stringResource(R.string.mock_test_rating_good)
                        MockTestPerformanceRating.NEEDS_IMPROVEMENT -> stringResource(R.string.mock_test_rating_needs_improvement)
                    }
                    val ratingDesc = when (result.performanceRating) {
                        MockTestPerformanceRating.EXCELLENT -> stringResource(R.string.mock_test_rating_excellent_desc)
                        MockTestPerformanceRating.GOOD -> stringResource(R.string.mock_test_rating_good_desc)
                        MockTestPerformanceRating.NEEDS_IMPROVEMENT -> stringResource(R.string.mock_test_rating_needs_improvement_desc)
                    }
                    val ratingColor = when (result.performanceRating) {
                        MockTestPerformanceRating.EXCELLENT -> SuccessGreen
                        MockTestPerformanceRating.GOOD -> MaterialTheme.colorScheme.primary
                        MockTestPerformanceRating.NEEDS_IMPROVEMENT -> MaterialTheme.colorScheme.error
                    }

                    Surface(
                        shape = CircleShape,
                        color = ratingColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ratingColor.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("mock_test_performance_rating")
                    ) {
                        Text(
                            text = ratingLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ratingColor,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = ratingDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Timing Card (Time Used & Time Remaining)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_timing_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.spacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mock_test_metric_time_used")
                    ) {
                        Text(
                            text = stringResource(R.string.mock_test_result_time_used),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDuration(timeUsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (configuration.durationMinutes > 0) {
                        VerticalDivider(
                            modifier = Modifier.height(32.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mock_test_metric_time_remaining")
                        ) {
                            Text(
                                text = stringResource(R.string.mock_test_result_time_remaining),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDuration(timeRemainingSeconds),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Breakdown Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_breakdown_card"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    ResultRow(
                        label = stringResource(R.string.mock_test_result_total, result.totalQuestions),
                        badgeColor = MaterialTheme.colorScheme.primary,
                        tag = "mock_test_metric_total"
                    )
                    ResultRow(
                        label = stringResource(R.string.mock_test_result_answered, result.answeredQuestions),
                        badgeColor = MaterialTheme.colorScheme.secondary,
                        tag = "mock_test_metric_answered"
                    )
                    ResultRow(
                        label = stringResource(R.string.mock_test_result_unanswered, result.unansweredQuestions),
                        badgeColor = MaterialTheme.colorScheme.outline,
                        tag = "mock_test_metric_unanswered"
                    )
                    ResultRow(
                        label = stringResource(R.string.mock_test_result_correct, result.correctAnswers),
                        badgeColor = SuccessGreen,
                        tag = "mock_test_metric_correct"
                    )
                    ResultRow(
                        label = stringResource(R.string.mock_test_result_incorrect, result.incorrectAnswers),
                        badgeColor = MaterialTheme.colorScheme.error,
                        tag = "mock_test_metric_incorrect"
                    )
                }
            }

            // Question Outcome Summary (Requirement 3)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_outcome_summary"),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(dimensions.spacingMedium),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_outcome_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (result.questionOutcomes.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(result.questionOutcomes, key = { it.questionNumber }) { item ->
                                val (statusText, badgeColor) = when (item.status) {
                                    MockTestQuestionOutcomeStatus.CORRECT -> Pair(
                                        stringResource(R.string.mock_test_outcome_correct_badge),
                                        SuccessGreen
                                    )
                                    MockTestQuestionOutcomeStatus.INCORRECT -> Pair(
                                        stringResource(R.string.mock_test_outcome_incorrect_badge),
                                        MaterialTheme.colorScheme.error
                                    )
                                    MockTestQuestionOutcomeStatus.UNANSWERED -> Pair(
                                        stringResource(R.string.mock_test_outcome_unanswered_badge),
                                        MaterialTheme.colorScheme.outline
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(dimensions.cornerSmall),
                                    color = badgeColor.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.testTag("mock_test_outcome_item_${item.questionNumber}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = badgeColor,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Q${item.questionNumber}: $statusText",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // CTA Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
            ) {
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = dimensions.minTouchTarget)
                        .testTag("mock_test_restart_button"),
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_action_restart),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onBackToOverview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = dimensions.minTouchTarget)
                        .testTag("mock_test_back_to_overview_button"),
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_action_back_to_overview),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = dimensions.minTouchTarget)
                        .testTag("mock_test_done_button"),
                    shape = RoundedCornerShape(dimensions.cornerMedium)
                ) {
                    Text(
                        text = stringResource(R.string.mock_test_action_done),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, badgeColor: Color, tag: String = "") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tag.isNotEmpty()) Modifier.testTag(tag) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = badgeColor,
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}

/**
 * Question Palette Composable (Step 19).
 *
 * Displays a compact, horizontally scrollable list of question numbers
 * representing every question in the active mock test session.
 *
 * Distinguishes:
 * - Current question: Primary colored highlight
 * - Answered question: Distinct answered indicator
 * - Unanswered question: Neutral outline
 *
 * Guarantees:
 * - Does NOT display correct/incorrect state, answer keys, or explanations.
 * - Selecting any question navigates directly to that question.
 * - Auto-scrolls to keep the active question visible.
 */
@Composable
private fun MockTestQuestionPalette(
    paletteItems: List<MockTestPaletteItem>,
    onSelectQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val listState = rememberLazyListState()

    // Auto-scroll to ensure current question is visible
    val currentIndex = paletteItems.indexOfFirst { it.isCurrent }
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    Card(
        modifier = modifier.testTag("mock_test_question_palette"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            // Palette Header: Title and Status Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensions.spacingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.mock_test_palette_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Legend: Current, Answered, Unanswered
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PaletteLegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.mock_test_palette_current)
                    )
                    PaletteLegendItem(
                        color = SuccessGreen,
                        label = stringResource(R.string.mock_test_palette_answered)
                    )
                    PaletteLegendItem(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        label = stringResource(R.string.mock_test_palette_unanswered)
                    )
                }
            }

            // Horizontally Scrollable Question Numbers
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_palette_list"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                items(
                    items = paletteItems,
                    key = { it.questionNumber }
                ) { item ->
                    PaletteItemButton(
                        item = item,
                        onClick = { onSelectQuestion(item.index) }
                    )
                }
            }
        }
    }
}

/**
 * Compact legend dot and text for the question palette.
 */
@Composable
private fun PaletteLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Palette question button with distinct states: Current, Answered, Unanswered.
 */
@Composable
private fun PaletteItemButton(
    item: MockTestPaletteItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    // Determine state colors
    val (containerColor, contentColor, borderColor) = when {
        item.isCurrent -> {
            Triple(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.onPrimary,
                MaterialTheme.colorScheme.primary
            )
        }
        item.isAnswered -> {
            Triple(
                SuccessGreen.copy(alpha = 0.15f),
                SuccessGreen,
                SuccessGreen.copy(alpha = 0.6f)
            )
        }
        else -> {
            Triple(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.onSurfaceVariant,
                MaterialTheme.colorScheme.outlineVariant
            )
        }
    }

    val stateDesc = when {
        item.isCurrent && item.isAnswered -> "current, answered"
        item.isCurrent -> "current, unanswered"
        item.isAnswered -> "answered"
        else -> "unanswered"
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(38.dp)
            .testTag("palette_item_${item.questionNumber}")
            .semantics {
                role = Role.Button
                selected = item.isCurrent
                contentDescription = "Question ${item.questionNumber}, $stateDesc"
            },
        shape = RoundedCornerShape(dimensions.cornerSmall),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = if (item.isCurrent) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${item.questionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            // Small dot indicator if current AND already answered
            if (item.isCurrent && item.isAnswered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

/**
 * Compact status summary in Active Test UI (Step 20).
 *
 * Displays:
 * - Total Questions
 * - Answered
 * - Unanswered
 *
 * Sourced directly from [MockTestUiState.ActiveTest] (single source of truth).
 */
@Composable
private fun MockTestStatusSummary(
    totalQuestions: Int,
    answeredCount: Int,
    unansweredCount: Int,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mock_test_status_summary"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusSummaryMetric(
                label = stringResource(R.string.mock_test_stat_label_total),
                value = "$totalQuestions",
                contentColor = MaterialTheme.colorScheme.onSurface,
                testTag = "mock_test_stat_total"
            )

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            StatusSummaryMetric(
                label = stringResource(R.string.mock_test_stat_label_answered),
                value = "$answeredCount",
                contentColor = SuccessGreen,
                testTag = "mock_test_stat_answered"
            )

            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            StatusSummaryMetric(
                label = stringResource(R.string.mock_test_stat_label_unanswered),
                value = "$unansweredCount",
                contentColor = if (unansweredCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                testTag = "mock_test_stat_unanswered"
            )
        }
    }
}

@Composable
private fun StatusSummaryMetric(
    label: String,
    value: String,
    contentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


