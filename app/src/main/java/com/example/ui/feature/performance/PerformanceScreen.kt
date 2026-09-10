package com.example.ui.feature.performance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.performance.StudentPerformance
import com.example.domain.model.performance.SubtopicPerformance
import com.example.ui.components.AppCard
import com.example.ui.feature.practice.components.PracticeAttemptHistorySection
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.SuccessGreen
import java.util.Locale

/**
 * Screen displaying student progress and aggregated performance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerformanceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dimensions = LocalDimensions.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("performance_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.performance_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("performance_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = dimensions.maxContentWidth)
            ) {
                when (val state = uiState) {
                    is PerformanceUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("performance_loading"),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    is PerformanceUiState.Empty -> {
                        PerformanceEmptyView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensions.spacingLarge)
                        )
                    }

                    is PerformanceUiState.Error -> {
                        PerformanceErrorView(
                            errorMessage = state.message,
                            onRetry = { viewModel.retry() },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensions.spacingLarge)
                        )
                    }

                    is PerformanceUiState.Success -> {
                        PerformanceContentView(
                            performance = state.performance,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Main content view displaying overall metrics, subtopic breakdown, and recent attempts.
 */
@Composable
private fun PerformanceContentView(
    performance: StudentPerformance,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    LazyColumn(
        modifier = modifier
            .testTag("performance_content"),
        contentPadding = PaddingValues(dimensions.spacingMedium),
        verticalArrangement = Arrangement.spacedBy(dimensions.spacingLarge)
    ) {
        // 1. Overall Performance Summary Card
        item {
            OverallPerformanceCard(performance = performance)
        }

        // 2. Subtopic Performance Breakdown
        if (performance.subtopicPerformances.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.performance_subtopic_breakdown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(
                items = performance.subtopicPerformances,
                key = { it.subtopicId }
            ) { subtopic ->
                SubtopicPerformanceCard(subtopic = subtopic)
            }
        }

        // 3. Recent Performance Section (Reusing Step 11 Practice Attempt component)
        if (performance.recentAttempts.isNotEmpty()) {
            item {
                PracticeAttemptHistorySection(
                    attempts = performance.recentAttempts,
                    maxDisplayCount = 5
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimensions.spacingMedium))
        }
    }
}

/**
 * Overview card with primary metrics: Completed Practices, Questions Attempted,
 * Correct Answers, Incorrect Answers, Overall Accuracy.
 */
@Composable
private fun OverallPerformanceCard(
    performance: StudentPerformance,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    AppCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "overall_performance_card"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.performance_overview_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Accuracy Badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(dimensions.cornerPill)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", performance.overallAccuracyPercentage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingExtraSmall)
                            .testTag("stat_overall_accuracy")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Metrics Grid: 2 rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = stringResource(R.string.performance_completed_practices),
                    value = performance.totalCompletedAttempts.toString(),
                    testTag = "stat_completed_practices",
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = stringResource(R.string.performance_questions_attempted),
                    value = performance.totalQuestionsAttempted.toString(),
                    testTag = "stat_questions_attempted",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = stringResource(R.string.performance_correct_answers),
                    value = performance.totalCorrectAnswers.toString(),
                    valueColor = SuccessGreen,
                    testTag = "stat_correct_answers",
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    label = stringResource(R.string.performance_incorrect_answers),
                    value = performance.totalIncorrectAnswers.toString(),
                    valueColor = ErrorRed,
                    testTag = "stat_incorrect_answers",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual metric display item.
 */
@Composable
private fun MetricItem(
    label: String,
    value: String,
    testTag: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val dimensions = LocalDimensions.current
    Column(
        modifier = modifier.padding(horizontal = dimensions.spacingExtraSmall),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            modifier = Modifier.testTag(testTag)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card displaying aggregated performance for a specific subtopic.
 * Flow: Subtopic -> Attempts -> Questions -> Accuracy
 */
@Composable
private fun SubtopicPerformanceCard(
    subtopic: SubtopicPerformance,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val displayName = subtopic.subtopicTitle ?: subtopic.subtopicId

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subtopic_perf_${subtopic.subtopicId}"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            // Header: Subtopic Title & Accuracy Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(dimensions.cornerPill)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", subtopic.accuracyPercentage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = dimensions.spacingSmall, vertical = 2.dp)
                    )
                }
            }

            // Stats Row: Attempts -> Questions -> Correct/Incorrect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.performance_attempts_count, subtopic.attemptsCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = stringResource(R.string.performance_questions_count, subtopic.questionsAnswered),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "${subtopic.correctAnswers} Correct",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = SuccessGreen
                )
            }
        }
    }
}

/**
 * Friendly, meaningful empty state shown when no practice attempts exist.
 */
@Composable
private fun PerformanceEmptyView(
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier.testTag("performance_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(dimensions.cornerPill),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

        Text(
            text = stringResource(R.string.performance_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = stringResource(R.string.performance_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = dimensions.spacingMedium)
        )
    }
}

/**
 * Error view with retry capability.
 */
@Composable
private fun PerformanceErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier.testTag("performance_error"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(dimensions.spacingMedium))

        Text(
            text = stringResource(R.string.performance_load_error),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = dimensions.spacingMedium)
        )

        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

        Button(
            onClick = onRetry,
            modifier = Modifier.testTag("performance_retry_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingSmall))
            Text(text = stringResource(R.string.performance_retry))
        }
    }
}
