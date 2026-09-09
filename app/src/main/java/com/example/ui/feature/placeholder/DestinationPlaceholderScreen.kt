package com.example.ui.feature.placeholder

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.navigation.ShellDestination
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyState
import com.example.ui.components.FeatureCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalDimensions

data class PlaceholderPreviewItem(
    val title: String,
    val description: String,
    val tag: String? = null
)

/**
 * Generic, clean placeholder screen for future educational modules.
 * Demonstrates the reusable component design system while keeping educational
 * feature implementation strictly deferred to future steps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationPlaceholderScreen(
    destination: ShellDestination,
    onNavigateBack: (() -> Unit)? = null,
    onItemClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current
    val title = stringResource(destination.titleResId)

    val previewItems = getPlaceholderItemsForDestination(destination)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("placeholder_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_navigation)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

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
                // Architectural Notice Banner
                item {
                    AppCard(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                        testTag = "placeholder_banner"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(dimensions.spacingSmall)
                                        .size(dimensions.iconSmall),
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(dimensions.spacingMedium))
                            Column {
                                Text(
                                    text = "$title Foundation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                                Text(
                                    text = stringResource(R.string.placeholder_notice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section Header
                item {
                    SectionHeader(
                        title = "Upcoming $title Modules",
                        subtitle = "Curriculum structures prepared for offline database sync in upcoming steps."
                    )
                }

                // Feature preview cards using reusable FeatureCard component
                items(previewItems) { item ->
                    FeatureCard(
                        title = item.title,
                        description = item.description,
                        icon = destination.selectedIcon,
                        tag = item.tag,
                        onClick = { onItemClick?.invoke(item.title) },
                        testTag = "preview_card_${item.title.lowercase().replace(" ", "_")}"
                    )
                }

                // Empty state demonstration using reusable EmptyState component
                item {
                    EmptyState(
                        icon = destination.unselectedIcon,
                        title = "Content Pipeline Ready",
                        message = "Local caching and Room persistence for $title will be configured when content schemas are implemented.",
                        testTag = "placeholder_empty_state"
                    )
                }
            }
        }
    }
}

private fun getPlaceholderItemsForDestination(destination: ShellDestination): List<PlaceholderPreviewItem> {
    return when (destination) {
        ShellDestination.Exams -> listOf(
            PlaceholderPreviewItem(
                title = "National Competitive Exam Track",
                description = "Full syllabus breakdown with Physics, Chemistry, and Mathematics tracks.",
                tag = "Active Track"
            ),
            PlaceholderPreviewItem(
                title = "State Level Entrance Series",
                description = "Regional syllabus variations, chapter weightage tables, and test series.",
                tag = "Preview"
            ),
            PlaceholderPreviewItem(
                title = "Graduate Aptitude & Foundation Test",
                description = "Conceptual reasoning, engineering fundamentals, and quantitative tracks.",
                tag = "Upcoming"
            )
        )
        ShellDestination.Practice -> listOf(
            PlaceholderPreviewItem(
                title = "Thermodynamics & Kinetic Theory",
                description = "Topic-wise multiple choice problem sets with step-by-step solution guides.",
                tag = "High Yield"
            ),
            PlaceholderPreviewItem(
                title = "Electrostatics & Magnetism",
                description = "Formula-oriented numerical exercises with accuracy and speed feedback.",
                tag = "65 Problems"
            ),
            PlaceholderPreviewItem(
                title = "Organic Synthesis & Mechanisms",
                description = "Reaction pathways, reagents reference tables, and interactive drills.",
                tag = "50 Problems"
            )
        )
        ShellDestination.MockTests -> listOf(
            PlaceholderPreviewItem(
                title = "All-India Diagnostic Mock Test #01",
                description = "Timed simulation with negative marking and percentile projection.",
                tag = "Full Length"
            ),
            PlaceholderPreviewItem(
                title = "Speed & Accuracy Sprint #04",
                description = "45-minute rapid recall test targeting high-frequency examination topics.",
                tag = "45 Mins"
            ),
            PlaceholderPreviewItem(
                title = "Subject-Wise Comprehensive Mock: Physics",
                description = "Targeted 30-question diagnostic evaluating Mechanics and Modern Physics.",
                tag = "Subject Drill"
            )
        )
        ShellDestination.EBooks -> listOf(
            PlaceholderPreviewItem(
                title = "Essential Physics Formula Handbook",
                description = "Pocket revision handbook with key definitions, units, and derivation tips.",
                tag = "Offline PDF"
            ),
            PlaceholderPreviewItem(
                title = "Chemistry Mnemonics & Reaction Guide",
                description = "Concise memory aids and structured reaction charts for fast revision.",
                tag = "Textbook"
            ),
            PlaceholderPreviewItem(
                title = "Previous 10 Years Question Compendium",
                description = "Subject-categorized archive of verified past exam questions and keys.",
                tag = "Archive"
            )
        )
        ShellDestination.Videos -> listOf(
            PlaceholderPreviewItem(
                title = "Mastering Rotational Motion in 60 Minutes",
                description = "Conceptual visual breakdown with free-body diagrams and torque analysis.",
                tag = "Lecture"
            ),
            PlaceholderPreviewItem(
                title = "Thermodynamics Cycle Analysis Walkthrough",
                description = "Step-by-step solution of Carnot cycle and heat pump examination problems.",
                tag = "Tutorial"
            ),
            PlaceholderPreviewItem(
                title = "High-Yield Organic Mechanisms Primer",
                description = "Interactive reaction pathways with common traps and shortcuts highlighted.",
                tag = "Quick Review"
            )
        )
        ShellDestination.ProfileSettings -> listOf(
            PlaceholderPreviewItem(
                title = "Study Schedule & Notifications",
                description = "Daily revision streak alerts, study reminders, and exam date countdowns.",
                tag = "Preferences"
            ),
            PlaceholderPreviewItem(
                title = "Appearance & Theme",
                description = "Select Light, Dark, or System dynamic Material 3 color themes.",
                tag = "Theme"
            ),
            PlaceholderPreviewItem(
                title = "Offline Storage & Local Cache",
                description = "Manage downloaded textbooks, offline problem sets, and Room database cache.",
                tag = "Storage"
            ),
            PlaceholderPreviewItem(
                title = "Accessibility & Text Scaling",
                description = "Adjust text size, contrast levels, and interactive touch targets.",
                tag = "Accessibility"
            )
        )
        else -> emptyList()
    }
}
