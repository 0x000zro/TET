package com.example.ui.feature.shell

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.ArchitecturalLayerInfo
import com.example.navigation.ShellDestination
import com.example.ui.components.AppCard
import com.example.ui.components.FeatureCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalDimensions
import com.example.ui.theme.SuccessGreen

/**
 * Screen content for the "More" tab, hosting secondary destinations
 * (eBooks, Videos, Profile/Settings) and preserving architectural specifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTabContent(
    uiState: MainShellUiState,
    onNavigateToDestination: (ShellDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.nav_more),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
                // Secondary Educational Modules
                item {
                    SectionHeader(
                        title = "Secondary Modules",
                        subtitle = "Additional learning materials and settings."
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.nav_performance),
                        description = "Track practice attempts, answered questions, accuracy, and subtopic progress",
                        icon = Icons.Default.Assessment,
                        tag = "Analytics",
                        onClick = { onNavigateToDestination(ShellDestination.Performance) },
                        testTag = "more_performance_card"
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.nav_ebooks),
                        description = "Digital textbooks, formula compendiums, and revision handbooks",
                        icon = Icons.Default.MenuBook,
                        tag = "Library",
                        onClick = { onNavigateToDestination(ShellDestination.EBooks) },
                        testTag = "more_ebooks_card"
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.nav_videos),
                        description = "Conceptual lecture recordings, derivations, and problem breakdowns",
                        icon = Icons.Default.PlayCircle,
                        tag = "Media",
                        onClick = { onNavigateToDestination(ShellDestination.Videos) },
                        testTag = "more_videos_card"
                    )
                }

                item {
                    FeatureCard(
                        title = stringResource(R.string.nav_profile_settings),
                        description = "Study notification reminders, appearance theme, and offline storage",
                        icon = Icons.Default.AccountCircle,
                        tag = "Preferences",
                        onClick = { onNavigateToDestination(ShellDestination.ProfileSettings) },
                        testTag = "more_profile_settings_card"
                    )
                }

                // Architecture and Foundation Status Section
                item {
                    Spacer(modifier = Modifier.height(dimensions.spacingSmall))
                    SectionHeader(
                        title = "Foundation & Architecture Spec",
                        subtitle = "Verified Clean Architecture boundaries ready for Step 3."
                    )
                }

                item {
                    FoundationStatusCard(info = uiState.foundationInfo)
                }

                item {
                    SectionHeader(
                        title = "Architectural Layers",
                        subtitle = "Separation of concerns enforced across the codebase."
                    )
                }

                val layers = uiState.foundationInfo?.layers ?: emptyList()
                items(layers) { layer ->
                    ArchitecturalLayerCard(layer = layer)
                }
            }
        }
    }
}

@Composable
private fun FoundationStatusCard(
    info: AppFoundationInfo?,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    AppCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        modifier = modifier.testTag("foundation_status_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(dimensions.spacingSmall)
                            .size(dimensions.iconMedium),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(dimensions.spacingMedium))
                Column {
                    Text(
                        text = stringResource(R.string.foundation_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Native Android Jetpack Compose & Clean Architecture",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            Text(
                text = stringResource(R.string.foundation_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ArchitecturalLayerCard(
    layer: ArchitecturalLayerInfo,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalDimensions.current

    AppCard(
        modifier = modifier.testTag("layer_card_${layer.layerName.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(dimensions.cornerSmall)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            layer.layerName.contains("Data") || layer.layerName.contains("Database") -> Icons.Default.Storage
                            layer.layerName.contains("UI") || layer.layerName.contains("Shell") -> Icons.Default.GridView
                            else -> Icons.Default.Layers
                        },
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconMedium),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimensions.spacingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = layer.layerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                Text(
                    text = layer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(dimensions.spacingExtraSmall))
                Text(
                    text = layer.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
