package com.learningblueprint.core.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SocialBrandIcon(
    platform: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 44.dp
) {
    val key = platform.trim().uppercase()

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            // WHATSAPP: Authentic Green Badge with Speech Bubble & Handset
            key == "WA" || key.contains("WHATSAPP") -> {
                Canvas(modifier = Modifier.size(sizeDp)) {
                    val w = size.width
                    val h = size.height

                    // Background Squircle
                    drawRoundRect(
                        color = Color(0xFF25D366),
                        cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
                    )

                    // Speech Bubble Path
                    val bubblePath = Path().apply {
                        moveTo(w * 0.5f, h * 0.22f)
                        cubicTo(w * 0.32f, h * 0.22f, w * 0.20f, h * 0.34f, w * 0.20f, h * 0.50f)
                        cubicTo(w * 0.20f, h * 0.57f, w * 0.23f, h * 0.63f, w * 0.27f, h * 0.68f)
                        lineTo(w * 0.23f, h * 0.78f)
                        lineTo(w * 0.35f, h * 0.74f)
                        cubicTo(w * 0.40f, h * 0.77f, w * 0.45f, h * 0.78f, w * 0.50f, h * 0.78f)
                        cubicTo(w * 0.68f, h * 0.78f, w * 0.80f, h * 0.66f, w * 0.80f, h * 0.50f)
                        cubicTo(w * 0.80f, h * 0.34f, w * 0.68f, h * 0.22f, w * 0.50f, h * 0.22f)
                        close()
                    }
                    drawPath(bubblePath, color = Color.White)

                    // Inner Phone Receiver Curve
                    val phonePath = Path().apply {
                        moveTo(w * 0.36f, h * 0.42f)
                        cubicTo(w * 0.38f, h * 0.38f, w * 0.42f, h * 0.40f, w * 0.44f, h * 0.44f)
                        lineTo(w * 0.46f, h * 0.48f)
                        cubicTo(w * 0.47f, h * 0.51f, w * 0.46f, h * 0.53f, w * 0.44f, h * 0.55f)
                        cubicTo(w * 0.48f, h * 0.62f, w * 0.54f, h * 0.65f, w * 0.58f, h * 0.67f)
                        cubicTo(w * 0.60f, h * 0.65f, w * 0.62f, h * 0.63f, w * 0.65f, h * 0.65f)
                        lineTo(w * 0.69f, h * 0.67f)
                        cubicTo(w * 0.72f, h * 0.69f, w * 0.72f, h * 0.73f, w * 0.69f, h * 0.76f)
                        cubicTo(w * 0.66f, h * 0.79f, w * 0.59f, h * 0.79f, w * 0.49f, h * 0.69f)
                        cubicTo(w * 0.39f, h * 0.59f, w * 0.34f, h * 0.48f, w * 0.36f, h * 0.42f)
                        close()
                    }
                    drawPath(phonePath, color = Color(0xFF25D366))
                }
            }

            // YOUTUBE: Official Red Rounded Rect with Centered Play Triangle
            key == "YT" || key.contains("YOUTUBE") -> {
                Canvas(modifier = Modifier.size(sizeDp)) {
                    val w = size.width
                    val h = size.height

                    // Red Background
                    drawRoundRect(
                        color = Color(0xFFFF0000),
                        cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
                    )

                    // White Centered Play Triangle
                    val playPath = Path().apply {
                        moveTo(w * 0.42f, h * 0.33f)
                        lineTo(w * 0.66f, h * 0.50f)
                        lineTo(w * 0.42f, h * 0.67f)
                        close()
                    }
                    drawPath(playPath, color = Color.White)
                }
            }

            // X (TWITTER): Crisp White Mathematical Diagonal Cross on True Black
            key == "X" || key.contains("TWITTER") -> {
                Canvas(modifier = Modifier.size(sizeDp)) {
                    val w = size.width
                    val h = size.height

                    // True Black Background
                    drawRoundRect(
                        color = Color(0xFF000000),
                        cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
                    )

                    // X Main Diagonal (\)
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.28f, h * 0.26f),
                        end = Offset(w * 0.72f, h * 0.74f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )

                    // X Second Diagonal (/)
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.72f, h * 0.26f),
                        end = Offset(w * 0.28f, h * 0.74f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // FACEBOOK: Official Royal Blue Badge with Lowercase 'f'
            key == "FB" || key.contains("FACEBOOK") -> {
                Canvas(modifier = Modifier.size(sizeDp)) {
                    val w = size.width
                    val h = size.height

                    // Royal Blue Background
                    drawRoundRect(
                        color = Color(0xFF1877F2),
                        cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
                    )

                    // Bold 'f' Letter Path
                    val fPath = Path().apply {
                        moveTo(w * 0.56f, h * 0.82f)
                        lineTo(w * 0.56f, h * 0.52f)
                        lineTo(w * 0.66f, h * 0.52f)
                        lineTo(w * 0.68f, h * 0.40f)
                        lineTo(w * 0.56f, h * 0.40f)
                        lineTo(w * 0.56f, h * 0.32f)
                        cubicTo(w * 0.56f, h * 0.26f, w * 0.60f, h * 0.24f, w * 0.67f, h * 0.24f)
                        lineTo(w * 0.72f, h * 0.24f)
                        lineTo(w * 0.72f, h * 0.14f)
                        lineTo(w * 0.62f, h * 0.14f)
                        cubicTo(w * 0.48f, h * 0.14f, w * 0.44f, h * 0.22f, w * 0.44f, h * 0.31f)
                        lineTo(w * 0.44f, h * 0.40f)
                        lineTo(w * 0.34f, h * 0.40f)
                        lineTo(w * 0.34f, h * 0.52f)
                        lineTo(w * 0.44f, h * 0.52f)
                        lineTo(w * 0.44f, h * 0.82f)
                        close()
                    }
                    drawPath(fPath, color = Color.White)
                }
            }

            // INSTAGRAM: Authentic Purple-Red-Yellow Radial Sunset Gradient & Camera Glyph
            else -> {
                Canvas(modifier = Modifier.size(sizeDp)) {
                    val w = size.width
                    val h = size.height

                    // Sunset Gradient Background
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF833AB4),
                                Color(0xFFFD1D1D),
                                Color(0xFFFCB045)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        ),
                        cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
                    )

                    // Camera Outer Outline
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.26f, h * 0.26f),
                        size = Size(w * 0.48f, h * 0.48f),
                        cornerRadius = CornerRadius(w * 0.12f, h * 0.12f),
                        style = Stroke(width = w * 0.055f)
                    )

                    // Camera Center Lens
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.12f,
                        center = Offset(w * 0.5f, h * 0.5f),
                        style = Stroke(width = w * 0.055f)
                    )

                    // Camera Flash Dot
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.025f,
                        center = Offset(w * 0.63f, h * 0.37f)
                    )
                }
            }
        }
    }
}
