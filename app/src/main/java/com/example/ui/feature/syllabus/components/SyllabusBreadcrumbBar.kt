package com.example.ui.feature.syllabus.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.feature.syllabus.SyllabusNavigationLevel
import com.example.ui.theme.LocalDimensions

/**
 * Horizontally scrollable breadcrumb navigation bar reflecting the active syllabus path:
 * Exams -> Paper -> Subject -> Topic -> Subtopic.
 */
@Composable
fun SyllabusBreadcrumbBar(
    currentLevel: SyllabusNavigationLevel,
    onNavigateToLevel: (SyllabusNavigationLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val scrollState = rememberScrollState()

    // Automatically scroll to the end when breadcrumb level advances
    LaunchedEffect(currentLevel) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("syllabus_breadcrumb_bar"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = dimensions.elevationLow
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = dimensions.spacingSmall, vertical = dimensions.spacingExtraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Root Breadcrumb: All Exams
            TextButton(
                onClick = { onNavigateToLevel(SyllabusNavigationLevel.Exams) },
                modifier = Modifier.testTag("breadcrumb_item_exams")
            ) {
                Text(
                    text = stringResource(R.string.syllabus_breadcrumb_home),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (currentLevel is SyllabusNavigationLevel.Exams) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (currentLevel is SyllabusNavigationLevel.Exams) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Exam level breadcrumb
            when (currentLevel) {
                is SyllabusNavigationLevel.Papers -> {
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name })
                }
                is SyllabusNavigationLevel.Subjects -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name })
                }
                is SyllabusNavigationLevel.Topics -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name })
                }
                is SyllabusNavigationLevel.Subtopics -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Topics(currentLevel.exam, currentLevel.paper, currentLevel.subject)) },
                        tag = "breadcrumb_item_subject"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = currentLevel.topic.name)
                }
                is SyllabusNavigationLevel.Questions -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Topics(currentLevel.exam, currentLevel.paper, currentLevel.subject)) },
                        tag = "breadcrumb_item_subject"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.topic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subtopics(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic)) },
                        tag = "breadcrumb_item_topic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = currentLevel.subtopic.name)
                }
                is SyllabusNavigationLevel.QuestionDetail -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Topics(currentLevel.exam, currentLevel.paper, currentLevel.subject)) },
                        tag = "breadcrumb_item_subject"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.topic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subtopics(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic)) },
                        tag = "breadcrumb_item_topic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subtopic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Questions(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic, currentLevel.subtopic)) },
                        tag = "breadcrumb_item_subtopic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = stringResource(R.string.question_number_label, currentLevel.questionIndex))
                }
                is SyllabusNavigationLevel.QuestionPractice -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Topics(currentLevel.exam, currentLevel.paper, currentLevel.subject)) },
                        tag = "breadcrumb_item_subject"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.topic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subtopics(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic)) },
                        tag = "breadcrumb_item_topic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subtopic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Questions(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic, currentLevel.subtopic)) },
                        tag = "breadcrumb_item_subtopic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = stringResource(R.string.question_number_label, currentLevel.questionIndex),
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.QuestionDetail(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic, currentLevel.subtopic, currentLevel.questionId, currentLevel.questionIndex)) },
                        tag = "breadcrumb_item_question"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = stringResource(R.string.practice_breadcrumb_title))
                }
                is SyllabusNavigationLevel.SubtopicPractice -> {
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.exam.shortName.ifBlank { currentLevel.exam.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Papers(currentLevel.exam)) },
                        tag = "breadcrumb_item_exam"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.paper.shortName.ifBlank { currentLevel.paper.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subjects(currentLevel.exam, currentLevel.paper)) },
                        tag = "breadcrumb_item_paper"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subject.shortName.ifBlank { currentLevel.subject.name },
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Topics(currentLevel.exam, currentLevel.paper, currentLevel.subject)) },
                        tag = "breadcrumb_item_subject"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.topic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Subtopics(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic)) },
                        tag = "breadcrumb_item_topic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbClickableText(
                        text = currentLevel.subtopic.name,
                        onClick = { onNavigateToLevel(SyllabusNavigationLevel.Questions(currentLevel.exam, currentLevel.paper, currentLevel.subject, currentLevel.topic, currentLevel.subtopic)) },
                        tag = "breadcrumb_item_subtopic"
                    )
                    BreadcrumbDivider()
                    BreadcrumbCurrentText(text = stringResource(R.string.practice_session_title))
                }
                SyllabusNavigationLevel.Exams -> {
                    // No children at root level
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbDivider() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        modifier = Modifier
            .size(10.dp)
            .padding(horizontal = 1.dp),
        tint = MaterialTheme.colorScheme.outline
    )
}

@Composable
private fun BreadcrumbClickableText(
    text: String,
    onClick: () -> Unit,
    tag: String
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BreadcrumbCurrentText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}
