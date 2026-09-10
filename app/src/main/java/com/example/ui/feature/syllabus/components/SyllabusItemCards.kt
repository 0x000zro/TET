package com.example.ui.feature.syllabus.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.SubtopicWithDetails
import com.example.domain.model.Topic
import com.example.ui.theme.LocalDimensions

@Composable
fun ExamItemCard(
    exam: Exam,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("syllabus_exam_${exam.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.elevationLow
        ),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerSmall),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exam.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exam.shortName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerExtraSmall),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = exam.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (exam.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                    Text(
                        text = exam.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun PaperItemCard(
    paper: Paper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("syllabus_paper_${paper.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.elevationLow
        ),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerSmall),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = paper.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (paper.shortName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerExtraSmall),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = paper.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (paper.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                    Text(
                        text = paper.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SubjectItemCard(
    subject: Subject,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("syllabus_subject_${subject.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.elevationLow
        ),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerSmall),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subject.shortName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Surface(
                            shape = RoundedCornerShape(dimensions.cornerExtraSmall),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = subject.shortName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (subject.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                    Text(
                        text = subject.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun TopicItemCard(
    topic: Topic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("syllabus_topic_${topic.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.elevationLow
        ),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(dimensions.cornerSmall),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Topic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(dimensions.iconMedium)
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (topic.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                    Text(
                        text = topic.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SubtopicItemCard(
    item: SubtopicWithDetails,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val subtopic = item.subtopic
    val metadata = item.metadata

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(dimensions.cornerMedium))
                        .clickable(role = Role.Button, onClick = onClick)
                } else Modifier
            )
            .testTag("subtopic_card_${subtopic.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensions.elevationLow
        ),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.spacingMedium)
        ) {
            // Header Row: Subtopic title and optional study duration badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtopic.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (metadata != null && metadata.estimatedMinutes > 0) {
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Surface(
                        shape = RoundedCornerShape(dimensions.cornerPill),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.syllabus_estimated_duration, metadata.estimatedMinutes),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Description when available
            if (subtopic.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                Text(
                    text = subtopic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Learning Objective when available
            if (metadata != null && metadata.learningObjective.isNotBlank()) {
                Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(dimensions.spacingSmall),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Column {
                            Text(
                                text = stringResource(R.string.syllabus_learning_objective),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = metadata.learningObjective,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Short Revision Note when available
            if (metadata != null && metadata.shortNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                Surface(
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(dimensions.spacingSmall),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                        Column {
                            Text(
                                text = stringResource(R.string.syllabus_short_note),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = metadata.shortNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Content Availability indicator
            Surface(
                shape = RoundedCornerShape(dimensions.cornerSmall),
                color = if (item.hasQuestions) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subtopic_questions_count_${subtopic.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = dimensions.spacingMedium, vertical = dimensions.spacingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSmall),
                        tint = if (item.hasQuestions) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Text(
                        text = if (item.hasQuestions) {
                            stringResource(R.string.syllabus_questions_available, item.questionCount)
                        } else {
                            stringResource(R.string.syllabus_no_questions)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (item.hasQuestions) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.hasQuestions) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (item.hasQuestions && onClick != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = stringResource(R.string.question_view_questions),
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Box(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = modifier, contentAlignment = contentAlignment) {
        content()
    }
}
