package com.example.ui.feature.bookmark

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
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
import com.example.domain.model.bookmark.BookmarkedQuestionSummary
import com.example.ui.feature.question.QuestionDetailView
import com.example.ui.feature.question.QuestionViewModel
import com.example.ui.feature.question.components.DifficultyChip
import com.example.ui.theme.LocalDimensions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying the student's Bookmarked Questions / Saved Questions module (Step 14).
 *
 * Implements:
 * - Reactive listing of bookmarked questions with deterministic ordering (`bookmarkedAt DESC, questionId ASC`).
 * - Optional subtopic filter chips.
 * - Single source of truth question review reusing [QuestionDetailView].
 * - Correct answer and explanation displayed in intentional review context.
 * - Explicit bookmark removal action without affecting question or syllabus tables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkedQuestionsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarkedQuestionsViewModel = viewModel(),
    questionViewModel: QuestionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedQuestion by viewModel.selectedQuestion.collectAsState()
    val dimensions = LocalDimensions.current

    // Review mode: When a specific bookmarked question is opened
    if (selectedQuestion != null) {
        val summary = selectedQuestion!!
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("bookmark_review_screen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.bookmark_review_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.clearSelectedQuestion() },
                            modifier = Modifier.testTag("bookmark_review_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.bookmarks_back_to_list)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.removeBookmark(summary.bookmarkedQuestion.questionId) },
                            modifier = Modifier.testTag("bookmark_review_remove_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkRemove,
                                contentDescription = stringResource(R.string.bookmark_remove_action),
                                tint = MaterialTheme.colorScheme.error
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
                questionId = summary.bookmarkedQuestion.questionId,
                questionIndex = 1,
                viewModel = questionViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                reviewCorrectOptionId = summary.question.options.firstOrNull { it.isCorrect }?.id,
                reviewHeaderContent = {
                    BookmarkReviewHeader(
                        summary = summary,
                        onRemoveBookmark = { viewModel.removeBookmark(summary.bookmarkedQuestion.questionId) }
                    )
                }
            )
        }
        return
    }

    // List mode: Displaying all bookmarks with filter chips
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("bookmarks_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.bookmarks_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.bookmarks_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("bookmarks_back_button")
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
                is BookmarkedQuestionsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("bookmarks_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                is BookmarkedQuestionsUiState.Empty -> {
                    BookmarksEmptyView(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is BookmarkedQuestionsUiState.Error -> {
                    BookmarksErrorView(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is BookmarkedQuestionsUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Subtopic filter chips (if multiple subtopics present)
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
                                    label = { Text(stringResource(R.string.bookmarks_filter_all)) },
                                    modifier = Modifier.testTag("bookmark_filter_chip_all")
                                )

                                state.availableSubtopics.forEach { (subtopicId, subtopicName) ->
                                    FilterChip(
                                        selected = state.selectedSubtopicId == subtopicId,
                                        onClick = { viewModel.filterBySubtopic(subtopicId) },
                                        label = { Text(subtopicName) },
                                        modifier = Modifier.testTag("bookmark_filter_chip_$subtopicId")
                                    )
                                }
                            }
                        }

                        // Bookmarks list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("bookmarks_list"),
                            contentPadding = PaddingValues(
                                horizontal = dimensions.spacingMedium,
                                vertical = dimensions.spacingSmall
                            ),
                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                        ) {
                            items(
                                items = state.items,
                                key = { it.bookmarkedQuestion.questionId }
                            ) { item ->
                                BookmarkItemCard(
                                    summary = item,
                                    onReview = { viewModel.selectQuestionForReview(item) },
                                    onRemove = { viewModel.removeBookmark(item.bookmarkedQuestion.questionId) }
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
 * Individual card displaying a bookmarked question summary.
 */
@Composable
private fun BookmarkItemCard(
    summary: BookmarkedQuestionSummary,
    onReview: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(summary.bookmarkedQuestion.bookmarkedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .clickable(onClick = onReview)
            .testTag("bookmark_item_card_${summary.bookmarkedQuestion.questionId}"),
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
            // Badges Row: Subtopic, Difficulty, and Quick-remove Bookmark icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Surface(
                        shape = RoundedCornerShape(dimensions.cornerSmall),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = summary.subtopicName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DifficultyChip(difficulty = summary.question.difficulty)
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_remove_button_${summary.bookmarkedQuestion.questionId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.bookmark_remove_action),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Question Text Snippet
            Text(
                text = summary.question.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Metadata: Bookmarked Date & Review Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.bookmark_saved_on, formattedDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.bookmark_review_action),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Header banner shown when reviewing a specific bookmarked question.
 */
@Composable
private fun BookmarkReviewHeader(
    summary: BookmarkedQuestionSummary,
    onRemoveBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(summary.bookmarkedQuestion.bookmarkedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bookmark_review_header_card"),
        shape = RoundedCornerShape(dimensions.cornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Text(
                        text = stringResource(R.string.bookmark_revision_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = summary.subtopicName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = stringResource(R.string.bookmark_saved_on, formattedDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            OutlinedButton(
                onClick = onRemoveBookmark,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bookmark_remove_action_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(dimensions.iconSmall)
                )
                Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                Text(
                    text = stringResource(R.string.bookmark_remove_action),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Empty view displayed when the student has no bookmarked questions.
 */
@Composable
private fun BookmarksEmptyView(
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .padding(dimensions.spacingLarge)
            .testTag("bookmarks_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(dimensions.cornerLarge),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

        Text(
            text = stringResource(R.string.bookmarks_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = stringResource(R.string.bookmarks_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
    }
}

/**
 * Error view for the Bookmarked Questions screen with a retry action.
 */
@Composable
private fun BookmarksErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .padding(dimensions.spacingLarge)
            .testTag("bookmarks_error"),
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
            text = stringResource(R.string.bookmarks_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(dimensions.spacingLarge))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(dimensions.cornerMedium)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(dimensions.spacingSmall))
            Text(stringResource(R.string.retry_button))
        }
    }
}
