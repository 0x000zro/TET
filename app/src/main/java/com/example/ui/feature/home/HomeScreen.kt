package com.example.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.navigation.ShellDestination
import com.example.ui.components.AppCard
import com.example.ui.components.FeatureCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.SuccessGreen

/**
 * Production-quality Home Screen UI foundation.
 * Features realistic educational preparation structures without adding real content data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDestination: (ShellDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(dimensions.cornerSmall)),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                    Column {
                        Text(
                            text = stringResource(R.string.home_greeting),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            actions = {
                // Streak Counter Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(end = dimensions.spacingSmall)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = dimensions.spacingSmall,
                            vertical = dimensions.spacingExtraSmall
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Active streak",
                            tint = StreakFlame,
                            modifier = Modifier.size(dimensions.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(dimensions.spacingExtraSmall))
                        Text(
                            text = stringResource(R.string.home_streak),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Profile / Settings shortcut
                IconButton(
                    onClick = { onNavigateToDestination(ShellDestination.ProfileSettings) },
                    modifier = Modifier.testTag("home_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.nav_profile_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Responsive Scrollable Content Container
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.spacingMedium),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = dimensions.maxContentWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = dimensions.spacingMedium),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
            ) {
                // 1. Welcome Area / Hero Banner
                item {
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        testTag = "home_welcome_banner"
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(dimensions.spacingSmall)
                                            .size(dimensions.iconSmall),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(dimensions.spacingMedium))
                                Column {
                                    Text(
                                        text = stringResource(R.string.home_welcome_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Target: 42 Days to Examination",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                            Text(
                                text = stringResource(R.string.home_welcome_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // 2. Exam Selection Placeholder
                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.home_selected_exam_label),
                            actionLabel = stringResource(R.string.home_change_exam),
                            onActionClick = { onNavigateToDestination(ShellDestination.Exams) },
                            testTag = "section_exam_selection"
                        )
                        Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                        AppCard(
                            onClick = { onNavigateToDestination(ShellDestination.Exams) },
                            testTag = "home_selected_exam_card"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(dimensions.cornerSmall)),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            modifier = Modifier.size(dimensions.iconMedium),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(dimensions.spacingMedium))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.home_selected_exam_value),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                                    Text(
                                        text = "Physics, Chemistry & Mathematics Syllabus Active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(dimensions.spacingSmall))

                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = stringResource(R.string.home_change_exam),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(dimensions.iconMedium)
                                )
                            }
                        }
                    }
                }

                // 3. Continue Learning Placeholder
                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.home_continue_learning),
                            testTag = "section_continue_learning"
                        )
                        Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                        AppCard(
                            testTag = "home_continue_learning_card"
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "In Progress",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "65% Completed",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                                Text(
                                    text = stringResource(R.string.home_continue_topic),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))

                                Text(
                                    text = stringResource(R.string.home_continue_subtopic),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                                LinearProgressIndicator(
                                    progress = { 0.65f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(dimensions.cornerPill)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                                PrimaryButton(
                                    text = stringResource(R.string.home_resume_button),
                                    onClick = { onNavigateToDestination(ShellDestination.Practice) },
                                    leadingIcon = Icons.Default.PlayArrow,
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "resume_session_button"
                                )
                            }
                        }
                    }
                }

                // 4. Quick Access Placeholder
                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.home_quick_access),
                            actionLabel = stringResource(R.string.see_all),
                            onActionClick = { onNavigateToDestination(ShellDestination.More) },
                            testTag = "section_quick_access"
                        )
                        Spacer(modifier = Modifier.height(dimensions.spacingSmall))

                        Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)) {
                            FeatureCard(
                                title = stringResource(R.string.nav_mock_tests),
                                description = "Timed exam simulations with percentile scoring & accuracy feedback",
                                icon = Icons.Default.Timer,
                                tag = "12 Available",
                                onClick = { onNavigateToDestination(ShellDestination.MockTests) },
                                testTag = "quick_access_mock_tests"
                            )
                            FeatureCard(
                                title = stringResource(R.string.nav_practice),
                                description = "Chapter-wise conceptual problem sets with step-by-step solutions",
                                icon = Icons.Default.Quiz,
                                tag = "Topic Sets",
                                onClick = { onNavigateToDestination(ShellDestination.Practice) },
                                testTag = "quick_access_practice"
                            )
                            FeatureCard(
                                title = stringResource(R.string.nav_ebooks),
                                description = "Verified formula handbooks, short notes, and offline reference guides",
                                icon = Icons.Default.MenuBook,
                                tag = "Handbooks",
                                onClick = { onNavigateToDestination(ShellDestination.EBooks) },
                                testTag = "quick_access_ebooks"
                            )
                            FeatureCard(
                                title = stringResource(R.string.nav_videos),
                                description = "High-yield concept lectures and interactive animated tutorials",
                                icon = Icons.Default.PlayCircle,
                                tag = "Lectures",
                                onClick = { onNavigateToDestination(ShellDestination.Videos) },
                                testTag = "quick_access_videos"
                            )
                        }
                    }
                }

                // 5. Recent Activity Placeholder
                item {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.home_recent_activity),
                            testTag = "section_recent_activity"
                        )
                        Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                        AppCard(
                            testTag = "home_recent_activity_card"
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(dimensions.iconMedium)
                                    )
                                    Spacer(modifier = Modifier.width(dimensions.spacingSmall))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.home_recent_title),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.home_recent_subtitle),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                                SecondaryButton(
                                    text = stringResource(R.string.home_view_report),
                                    onClick = { onNavigateToDestination(ShellDestination.Performance) },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "review_summary_button"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
