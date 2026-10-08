package com.example.ui.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PomodoroMode
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Circular timer progress indicator that visually displays the remaining time for the current session.
 *
 * Features:
 * - Sweeping active arc indicating remaining session time
 * - Outer dial with precision graduation tick marks
 * - Subtle inner track for total session bounds
 * - Animated glowing endpoint bead showing current progress mark
 * - Center digital display with MM:SS formatted countdown
 * - Session status badge and remaining percentage pill
 * - Smooth state transitions and interactive click handler
 */
@Composable
fun CircularTimerProgressIndicator(
    secondsRemaining: Int,
    totalSeconds: Int,
    currentMode: PomodoroMode,
    isRunning: Boolean,
    isPaused: Boolean,
    modeColor: Color,
    modifier: Modifier = Modifier,
    dialSize: Dp = 190.dp,
    strokeWidth: Dp = 9.dp,
    onClick: () -> Unit = {}
) {
    // Remaining time fraction: 1.0 (start) down to 0.0 (session end)
    val remainingFraction = if (totalSeconds > 0) {
        (secondsRemaining.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedRemainingFraction by animateFloatAsState(
        targetValue = remainingFraction,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "remainingTimerProgress"
    )

    // Gentle pulse animation when timer is actively counting down
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val currentGlow = if (isRunning) pulseGlow else 0.7f

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val remainingPercentage = (remainingFraction * 100).toInt()

    val accessibilityLabel = "$timeFormatted remaining in ${currentMode.title}. " +
            if (isRunning) "Running" else if (isPaused) "Paused" else "Ready to start"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(dialSize)
            .clip(CircleShape)
            .semantics { contentDescription = accessibilityLabel }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = modeColor),
                onClick = onClick
            )
            .testTag("circular_timer_progress_indicator")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val strokePx = strokeWidth.toPx()
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            val radius = (size.minDimension - strokePx) / 2f

            // 1. Subtle Precision Graduation Tick Marks (60 ticks around the dial)
            val totalTicks = 60
            for (i in 0 until totalTicks) {
                val tickAngle = -90f + (i * 360f / totalTicks)
                val tickFraction = i.toFloat() / totalTicks
                val isMajorTick = i % 5 == 0

                val tickLength = if (isMajorTick) strokePx * 0.8f else strokePx * 0.45f
                val innerR = radius - (strokePx / 2f) - tickLength
                val outerR = radius - (strokePx / 2f) - 2f

                val rad = Math.toRadians(tickAngle.toDouble())
                val startX = center.x + innerR * cos(rad).toFloat()
                val startY = center.y + innerR * sin(rad).toFloat()
                val endX = center.x + outerR * cos(rad).toFloat()
                val endY = center.y + outerR * sin(rad).toFloat()

                val isWithinRemainingArc = tickFraction <= animatedRemainingFraction
                val tickColor = if (isWithinRemainingArc) {
                    if (isMajorTick) modeColor.copy(alpha = 0.55f * currentGlow)
                    else modeColor.copy(alpha = 0.28f * currentGlow)
                } else {
                    if (isMajorTick) DarkSurfaceVariant.copy(alpha = 0.6f)
                    else DarkSurfaceVariant.copy(alpha = 0.3f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajorTick) 1.8.dp.toPx() else 1.0.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Base Background Track Circle (shows full session scope)
            drawCircle(
                color = DarkSurfaceVariant.copy(alpha = 0.65f),
                radius = radius,
                center = center,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 3. Sweeping Active Arc displaying Remaining Time
            if (animatedRemainingFraction > 0f) {
                val sweepAngle = 360f * animatedRemainingFraction

                // Dynamic gradient brush from accent glow to primary mode color
                val arcBrush = Brush.sweepGradient(
                    0.0f to modeColor.copy(alpha = 0.75f),
                    0.5f to modeColor,
                    1.0f to modeColor,
                    center = center
                )

                // Outer soft halo when active
                if (isRunning) {
                    drawArc(
                        color = modeColor.copy(alpha = 0.22f * currentGlow),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokePx * 1.6f, cap = StrokeCap.Round)
                    )
                }

                // Primary remaining time arc
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 4. Glowing Leading Endpoint Bead (highlights the current remaining point)
                if (animatedRemainingFraction > 0.01f) {
                    val endAngleDeg = -90f + sweepAngle
                    val endRad = Math.toRadians(endAngleDeg.toDouble())
                    val beadX = center.x + radius * cos(endRad).toFloat()
                    val beadY = center.y + radius * sin(endRad).toFloat()
                    val beadCenter = Offset(beadX, beadY)

                    // Outer halo
                    drawCircle(
                        color = modeColor.copy(alpha = 0.45f * currentGlow),
                        radius = strokePx * 0.85f,
                        center = beadCenter
                    )

                    // Solid bright bead
                    drawCircle(
                        color = modeColor,
                        radius = strokePx * 0.55f,
                        center = beadCenter
                    )

                    // White luminous core
                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = strokePx * 0.28f,
                        center = beadCenter
                    )
                }
            }
        }

        // Inner Digital Display & Status Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            // Mode Icon & Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val modeIcon = when (currentMode) {
                    PomodoroMode.FOCUS -> Icons.Default.LocalFireDepartment
                    PomodoroMode.SHORT_BREAK -> Icons.Default.Coffee
                    PomodoroMode.LONG_BREAK -> Icons.Default.SelfImprovement
                }
                Icon(
                    imageVector = modeIcon,
                    contentDescription = null,
                    tint = modeColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (currentMode) {
                        PomodoroMode.FOCUS -> "FOCUS"
                        PomodoroMode.SHORT_BREAK -> "SHORT BREAK"
                        PomodoroMode.LONG_BREAK -> "LONG BREAK"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColor,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Main Remaining Time Countdown (MM:SS)
            Text(
                text = timeFormatted,
                fontSize = if (dialSize < 175.dp) 34.sp else 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                letterSpacing = 1.sp,
                modifier = Modifier.testTag("timer_countdown_text")
            )

            // Session Running / Paused Status
            Text(
                text = if (isRunning) "FOCUSING" else if (isPaused) "PAUSED" else "READY",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isRunning) modeColor else TextSecondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Remaining Percentage Pill / Subtitle
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkBackground.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "$remainingPercentage% LEFT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
