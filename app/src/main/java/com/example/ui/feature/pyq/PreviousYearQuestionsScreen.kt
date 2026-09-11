package com.example.ui.feature.pyq

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.pyq.PreviousYearQuestionSummary
import com.example.domain.model.pyq.PyqVerificationStatus
import com.example.ui.feature.question.QuestionDetailView
import com.example.ui.feature.question.QuestionViewModel
import com.example.ui.theme.LocalDimensions

/**
 * Screen displaying the student's Previous Year Questions (PYQ) module (Step 15).
 *
 * Implements:
 * - Reactive listing of authenticated PYQs sorted deterministically (Year DESC, Session ASC, Question ID ASC).
 * - Multi-dimensional filtering by Exam, Paper, Year, Session, and Verification Status.
 * - Single source of truth question review reusing [QuestionDetailView].
 * - Correct answer and explanation displayed in intentional review context with PYQ origin metadata header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousYearQuestionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PyqViewModel = viewModel(),
    questionViewModel: QuestionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPyq by viewModel.selectedPyq.collectAsState()
    val dimensions = LocalDimensions.current

    // Review mode: When a specific PYQ is opened for detailed inspection
    if (selectedPyq != null) {
        val summary = selectedPyq!!
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("pyq_review_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.pyq_review_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.clearSelectedPyq() },
                            modifier = Modifier.testTag("pyq_review_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.pyq_back_to_list)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            QuestionDetailView(
                questionId = summary.pyq.questionId,
                questionIndex = 1,
                viewModel = questionViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                reviewCorrectOptionId = summary.question.options.firstOrNull { it.isCorrect }?.id,
                reviewHeaderContent = {
                    PyqReviewHeader(summary = summary)
                }
            )
        }
        return
    }

    // List mode: Displaying all PYQs with comprehensive filter chips
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pyq_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.pyq_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.pyq_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("pyq_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PyqUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pyq_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is PyqUiState.Empty -> {
                    PyqEmptyView(
                        isFiltered = state.isFiltered,
                        onClearFilters = { viewModel.clearFilters() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is PyqUiState.Error -> {
                    PyqErrorView(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is PyqUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Horizontal filter chips
                        PyqFilterSection(
                            state = state,
                            viewModel = viewModel
                        )

                        // Results Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingExtraSmall),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.pyq_total_count, state.totalCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (state.hasActiveFilters) {
                                TextButton(
                                    onClick = { viewModel.clearFilters() },
                                    modifier = Modifier.testTag("pyq_clear_filters_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.pyq_clear_filters),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        // PYQ list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("pyq_list"),
                            contentPadding = PaddingValues(
                                horizontal = dimensions.spacingMedium,
                                vertical = dimensions.spacingSmall
                            ),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            items(
                                items = state.items,
                                key = { it.pyq.id }
                            ) { item ->
                                PyqItemCard(
                                    summary = item,
                                    onReview = { viewModel.selectPyqForReview(item) }
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
 * Filter chips container supporting Exam, Paper, Year, Session, and Status filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PyqFilterSection(
    state: PyqUiState.Success,
    viewModel: PyqViewModel
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(vertical = dimensions.spacingExtraSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = dimensions.spacingMedium),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
        ) {
            // Verification status filter chips
            FilterChip(
                selected = state.selectedVerificationStatus == null,
                onClick = { viewModel.filterByVerificationStatus(null) },
                label = { Text(stringResource(R.string.pyq_filter_all_statuses)) },
                modifier = Modifier.testTag("pyq_filter_chip_status_all")
            )

            FilterChip(
                selected = state.selectedVerificationStatus == PyqVerificationStatus.VERIFIED,
                onClick = {
                    val newStatus = if (state.selectedVerificationStatus == PyqVerificationStatus.VERIFIED) null else PyqVerificationStatus.VERIFIED
                    viewModel.filterByVerificationStatus(newStatus)
                },
                label = { Text(stringResource(R.string.pyq_filter_verified_only)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (state.selectedVerificationStatus == PyqVerificationStatus.VERIFIED) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            Color(0xFF2E7D32)
                        }
                    )
                },
                modifier = Modifier.testTag("pyq_filter_chip_status_verified")
            )

            // Year filter chips
            if (state.availableYears.isNotEmpty()) {
                FilterChip(
                    selected = state.selectedYear == null,
                    onClick = { viewModel.filterByYear(null) },
                    label = { Text(stringResource(R.string.pyq_filter_all_years)) },
                    modifier = Modifier.testTag("pyq_filter_chip_year_all")
                )

                state.availableYears.forEach { year ->
                    FilterChip(
                        selected = state.selectedYear == year,
                        onClick = {
                            val newYear = if (state.selectedYear == year) null else year
                            viewModel.filterByYear(newYear)
                        },
                        label = { Text(year.toString()) },
                        modifier = Modifier.testTag("pyq_filter_chip_year_$year")
                    )
                }
            }

            // Exam filter chips (if multiple exams available)
            if (state.availableExams.size > 1) {
                FilterChip(
                    selected = state.selectedExamId == null,
                    onClick = { viewModel.filterByExam(null) },
                    label = { Text(stringResource(R.string.pyq_filter_all_exams)) },
                    modifier = Modifier.testTag("pyq_filter_chip_exam_all")
                )

                state.availableExams.forEach { (examId, examName) ->
                    FilterChip(
                        selected = state.selectedExamId == examId,
                        onClick = {
                            val newExam = if (state.selectedExamId == examId) null else examId
                            viewModel.filterByExam(newExam)
                        },
                        label = { Text(examName) },
                        modifier = Modifier.testTag("pyq_filter_chip_exam_$examId")
                    )
                }
            }

            // Paper filter chips (if multiple papers available)
            if (state.availablePapers.size > 1) {
                FilterChip(
                    selected = state.selectedPaperId == null,
                    onClick = { viewModel.filterByPaper(null) },
                    label = { Text(stringResource(R.string.pyq_filter_all_papers)) },
                    modifier = Modifier.testTag("pyq_filter_chip_paper_all")
                )

                state.availablePapers.forEach { (paperId, paperName) ->
                    FilterChip(
                        selected = state.selectedPaperId == paperId,
                        onClick = {
                            val newPaper = if (state.selectedPaperId == paperId) null else paperId
                            viewModel.filterByPaper(newPaper)
                        },
                        label = { Text(paperName) },
                        modifier = Modifier.testTag("pyq_filter_chip_paper_$paperId")
                    )
                }
            }

            // Session / Shift filter chips (if available)
            if (state.availableSessions.isNotEmpty()) {
                FilterChip(
                    selected = state.selectedSession == null,
                    onClick = { viewModel.filterBySession(null) },
                    label = { Text(stringResource(R.string.pyq_filter_all_sessions)) },
                    modifier = Modifier.testTag("pyq_filter_chip_session_all")
                )

                state.availableSessions.forEach { session ->
                    FilterChip(
                        selected = state.selectedSession == session,
                        onClick = {
                            val newSession = if (state.selectedSession == session) null else session
                            viewModel.filterBySession(newSession)
                        },
                        label = { Text(session) },
                        modifier = Modifier.testTag("pyq_filter_chip_session_$session")
                    )
                }
            }
        }
    }
}

/**
 * Individual card displaying a Previous Year Question summary item in the list.
 */
@Composable
private fun PyqItemCard(
    summary: PreviousYearQuestionSummary,
    onReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val isVerified = summary.pyq.verificationStatus == PyqVerificationStatus.VERIFIED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pyq_item_card_${summary.pyq.id}"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            // Top badges row: Year • Session, Verification Status, Exam/Paper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Year & Session badge
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    val yearSessionText = if (summary.pyq.session != null) {
                        stringResource(R.string.pyq_year_session_format, summary.pyq.year, summary.pyq.session)
                    } else {
                        summary.pyq.year.toString()
                    }
                    Text(
                        text = yearSessionText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Verification status badge
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Outlined.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isVerified) {
                                stringResource(R.string.pyq_verified_badge)
                            } else {
                                stringResource(R.string.pyq_unverified_badge)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Exam & Paper metadata tag
            val hierarchyTag = listOfNotNull(summary.examName, summary.paperName).joinToString(" • ")
            if (hierarchyTag.isNotEmpty()) {
                Text(
                    text = hierarchyTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
            }

            // Question text preview
            Text(
                text = summary.question.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Bottom row: Subtopic tag & Review action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Subtopic pill
                summary.subtopicName?.let { subtopic ->
                    Surface(
                        shape = RoundedCornerShape(dimensions.cornerPill),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = subtopic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } ?: Spacer(modifier = Modifier.width(1.dp))

                // Review button
                Button(
                    onClick = onReview,
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .testTag("pyq_review_action_${summary.pyq.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.wrong_questions_review_action),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * Top header displayed inside [QuestionDetailView] for a PYQ, showcasing origin exam, paper, year, session,
 * and verification metadata.
 */
@Composable
private fun PyqReviewHeader(
    summary: PreviousYearQuestionSummary,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val isVerified = summary.pyq.verificationStatus == PyqVerificationStatus.VERIFIED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = dimensions.spacingMedium)
            .testTag("pyq_review_header"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.pyq_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isVerified) stringResource(R.string.pyq_verified_badge) else stringResource(R.string.pyq_unverified_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Exam & Paper & Year & Session
            val detailsList = mutableListOf<String>()
            summary.examName?.let { detailsList.add(it) }
            summary.paperName?.let { detailsList.add(it) }
            val yearSession = if (summary.pyq.session != null) {
                "${summary.pyq.year} (${summary.pyq.session})"
            } else {
                "${summary.pyq.year}"
            }
            detailsList.add(yearSession)

            Text(
                text = detailsList.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Source attribution if present
            summary.pyq.source?.let { source ->
                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                Text(
                    text = stringResource(R.string.pyq_source_format, source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Empty state view when no previous year questions exist or match current filters.
 */
@Composable
private fun PyqEmptyView(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Box(
        modifier = modifier.testTag("pyq_empty"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(dimensions.spacingLarge)
                .widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerLarge),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = stringResource(R.string.pyq_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = stringResource(R.string.pyq_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isFiltered) {
                Spacer(modifier = Modifier.height(dimensions.spacingMedium))
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.testTag("pyq_empty_clear_filters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.pyq_clear_filters))
                }
            }
        }
    }
}

/**
 * Error state view when PYQ records fail to load.
 */
@Composable
private fun PyqErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Box(
        modifier = modifier.testTag("pyq_error"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(dimensions.spacingLarge)
                .widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = stringResource(R.string.pyq_error_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("pyq_retry_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.pyq_retry))
            }
        }
    }
}
