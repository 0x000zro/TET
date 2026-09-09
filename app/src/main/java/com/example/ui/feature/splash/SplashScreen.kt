package com.example.ui.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.LocalDimensions

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimensions = LocalDimensions.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(dimensions.spacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = dimensions.maxContentWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Emblem / Icon Container
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(dimensions.cornerLarge))
                    .testTag("splash_app_emblem"),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = dimensions.elevationHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingLarge))

            // App Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            // Tagline
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingMedium))

            // Foundation Status Badge
            Surface(
                modifier = Modifier.clip(CircleShape),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = dimensions.spacingMedium,
                        vertical = dimensions.spacingSmall
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(dimensions.iconSmall),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.size(dimensions.spacingExtraSmall))
                    Text(
                        text = stringResource(R.string.foundation_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensions.spacingExtraLarge))

            // Progress / Status indicator
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(dimensions.cornerPill))
                    .testTag("splash_progress_indicator"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(dimensions.spacingSmall))

            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(dimensions.spacingExtraLarge))

            // Action Button to proceed to Main Screen
            AnimatedVisibility(
                visible = uiState.isInitialized,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                com.example.ui.components.PrimaryButton(
                    text = stringResource(R.string.enter_dashboard),
                    onClick = onNavigateToMain,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    testTag = "enter_dashboard_button"
                )
            }
        }
    }
}
