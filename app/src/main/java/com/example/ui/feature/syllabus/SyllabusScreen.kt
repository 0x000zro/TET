package com.example.ui.feature.syllabus

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.feature.practice.PracticeSessionView
import com.example.ui.feature.practice.PracticeSessionViewModel
import com.example.ui.feature.practice.PracticeViewModel
import com.example.ui.feature.practice.QuestionPracticeView
import com.example.ui.feature.question.QuestionDetailView
import com.example.ui.feature.question.QuestionListView
import com.example.ui.feature.question.QuestionViewModel
import com.example.ui.feature.syllabus.components.ExamItemCard
import com.example.ui.feature.syllabus.components.PaperItemCard
import com.example.ui.feature.syllabus.components.SubjectItemCard
import com.example.ui.feature.syllabus.components.SubtopicItemCard
import com.example.ui.feature.syllabus.components.SyllabusBreadcrumbBar
import com.example.ui.feature.syllabus.components.SyllabusEmptyView
import com.example.ui.feature.syllabus.components.SyllabusErrorView
import com.example.ui.feature.syllabus.components.SyllabusLoadingView
import com.example.ui.feature.syllabus.components.TopicItemCard
import com.example.ui.theme.LocalDimensions

/**
 * Production-ready student-facing syllabus navigation screen.
 * Seamlessly guides the student across the structured hierarchy:
 * Exam -> Paper -> Subject -> Topic -> Subtopic -> Content Availability -> Questions -> Question Detail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    onNavigateBackToShell: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SyllabusViewModel = viewModel(),
    questionViewModel: QuestionViewModel = viewModel(),
    practiceViewModel: PracticeViewModel = viewModel(),
    practiceSessionViewModel: PracticeSessionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimensions = LocalDimensions.current

    // Android hardware/system back button handling
    BackHandler {
        if (!viewModel.navigateBack()) {
            onNavigateBackToShell()
        }
    }

    val currentTitle = when (val level = uiState.currentLevel) {
        SyllabusNavigationLevel.Exams -> stringResource(R.string.syllabus_exams_title)
        is SyllabusNavigationLevel.Papers -> level.exam.name
        is SyllabusNavigationLevel.Subjects -> level.paper.name
        is SyllabusNavigationLevel.Topics -> level.subject.name
        is SyllabusNavigationLevel.Subtopics -> level.topic.name
        is SyllabusNavigationLevel.Questions -> level.subtopic.name
        is SyllabusNavigationLevel.QuestionDetail -> stringResource(R.string.question_number_label, level.questionIndex)
        is SyllabusNavigationLevel.QuestionPractice -> stringResource(R.string.practice_title)
        is SyllabusNavigationLevel.SubtopicPractice -> stringResource(R.string.practice_session_title)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("syllabus_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!viewModel.navigateBack()) {
                                onNavigateBackToShell()
                            }
                        },
                        modifier = Modifier.testTag("syllabus_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_navigation)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Contextual Breadcrumb Bar
            SyllabusBreadcrumbBar(
                currentLevel = uiState.currentLevel,
                onNavigateToLevel = { targetLevel ->
                    viewModel.navigateToBreadcrumbLevel(targetLevel)
                }
            )

            // Dynamic hierarchy content container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Crossfade(
                    targetState = uiState.currentLevel,
                    label = "syllabus_level_crossfade"
                ) { level ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (level) {
                            SyllabusNavigationLevel.Exams -> {
                                when (val state = uiState.examsState) {
                                    SyllabusContentState.Loading -> SyllabusLoadingView()
                                    SyllabusContentState.Empty -> SyllabusEmptyView(
                                        message = stringResource(R.string.syllabus_empty_exams)
                                    )
                                    is SyllabusContentState.Error -> SyllabusErrorView(
                                        message = state.message,
                                        onRetry = { viewModel.retryCurrentLevel() }
                                    )
                                    is SyllabusContentState.Success -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = dimensions.maxContentWidth)
                                                .testTag("syllabus_list"),
                                            contentPadding = PaddingValues(dimensions.spacingMedium),
                                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                                        ) {
                                            items(state.items, key = { it.id }) { exam ->
                                                ExamItemCard(
                                                    exam = exam,
                                                    onClick = { viewModel.selectExam(exam) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is SyllabusNavigationLevel.Papers -> {
                                when (val state = uiState.papersState) {
                                    SyllabusContentState.Loading -> SyllabusLoadingView()
                                    SyllabusContentState.Empty -> SyllabusEmptyView(
                                        message = stringResource(R.string.syllabus_empty_papers)
                                    )
                                    is SyllabusContentState.Error -> SyllabusErrorView(
                                        message = state.message,
                                        onRetry = { viewModel.retryCurrentLevel() }
                                    )
                                    is SyllabusContentState.Success -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = dimensions.maxContentWidth)
                                                .testTag("syllabus_list"),
                                            contentPadding = PaddingValues(dimensions.spacingMedium),
                                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                                        ) {
                                            items(state.items, key = { it.id }) { paper ->
                                                PaperItemCard(
                                                    paper = paper,
                                                    onClick = { viewModel.selectPaper(paper) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is SyllabusNavigationLevel.Subjects -> {
                                when (val state = uiState.subjectsState) {
                                    SyllabusContentState.Loading -> SyllabusLoadingView()
                                    SyllabusContentState.Empty -> SyllabusEmptyView(
                                        message = stringResource(R.string.syllabus_empty_subjects)
                                    )
                                    is SyllabusContentState.Error -> SyllabusErrorView(
                                        message = state.message,
                                        onRetry = { viewModel.retryCurrentLevel() }
                                    )
                                    is SyllabusContentState.Success -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = dimensions.maxContentWidth)
                                                .testTag("syllabus_list"),
                                            contentPadding = PaddingValues(dimensions.spacingMedium),
                                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                                        ) {
                                            items(state.items, key = { it.id }) { subject ->
                                                SubjectItemCard(
                                                    subject = subject,
                                                    onClick = { viewModel.selectSubject(subject) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is SyllabusNavigationLevel.Topics -> {
                                when (val state = uiState.topicsState) {
                                    SyllabusContentState.Loading -> SyllabusLoadingView()
                                    SyllabusContentState.Empty -> SyllabusEmptyView(
                                        message = stringResource(R.string.syllabus_empty_topics)
                                    )
                                    is SyllabusContentState.Error -> SyllabusErrorView(
                                        message = state.message,
                                        onRetry = { viewModel.retryCurrentLevel() }
                                    )
                                    is SyllabusContentState.Success -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = dimensions.maxContentWidth)
                                                .testTag("syllabus_list"),
                                            contentPadding = PaddingValues(dimensions.spacingMedium),
                                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                                        ) {
                                            items(state.items, key = { it.id }) { topic ->
                                                TopicItemCard(
                                                    topic = topic,
                                                    onClick = { viewModel.selectTopic(topic) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is SyllabusNavigationLevel.Subtopics -> {
                                when (val state = uiState.subtopicsState) {
                                    SyllabusContentState.Loading -> SyllabusLoadingView()
                                    SyllabusContentState.Empty -> SyllabusEmptyView(
                                        message = stringResource(R.string.syllabus_empty_subtopics)
                                    )
                                    is SyllabusContentState.Error -> SyllabusErrorView(
                                        message = state.message,
                                        onRetry = { viewModel.retryCurrentLevel() }
                                    )
                                    is SyllabusContentState.Success -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = dimensions.maxContentWidth)
                                                .testTag("syllabus_list"),
                                            contentPadding = PaddingValues(dimensions.spacingMedium),
                                            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
                                        ) {
                                            items(state.items, key = { it.subtopic.id }) { subtopicItem ->
                                                SubtopicItemCard(
                                                    item = subtopicItem,
                                                    onClick = { viewModel.selectSubtopic(subtopicItem.subtopic) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is SyllabusNavigationLevel.Questions -> {
                                QuestionListView(
                                    subtopic = level.subtopic,
                                    viewModel = questionViewModel,
                                    onQuestionClick = { question ->
                                        viewModel.selectQuestion(question.id, question.questionNumber)
                                    },
                                    onStartPractice = {
                                        viewModel.startSubtopicPractice(level.subtopic)
                                    }
                                )
                            }

                            is SyllabusNavigationLevel.QuestionDetail -> {
                                QuestionDetailView(
                                    questionId = level.questionId,
                                    questionIndex = level.questionIndex,
                                    viewModel = questionViewModel,
                                    onPracticeClick = {
                                        viewModel.startQuestionPractice(level.questionId, level.questionIndex)
                                    }
                                )
                            }

                            is SyllabusNavigationLevel.QuestionPractice -> {
                                QuestionPracticeView(
                                    questionId = level.questionId,
                                    questionIndex = level.questionIndex,
                                    viewModel = practiceViewModel
                                )
                            }

                            is SyllabusNavigationLevel.SubtopicPractice -> {
                                PracticeSessionView(
                                    subtopicId = level.subtopic.id,
                                    viewModel = practiceSessionViewModel,
                                    onBackToSubtopic = {
                                        viewModel.selectSubtopic(level.subtopic)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
