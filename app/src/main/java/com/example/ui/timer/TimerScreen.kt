package com.example.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtTheme
import com.example.model.PomodoroMode
import com.example.ui.art.ArtCanvas
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    uiState: TimerUiState,
    availableThemes: List<ArtTheme>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSwitchMode: (PomodoroMode) -> Unit,
    onAddMinutes: (Int) -> Unit,
    onSelectArtTheme: (ArtTheme) -> Unit,
    onSetTaskTag: (String) -> Unit,
    onToggleFullScreen: () -> Unit,
    onDismissCompleteDialog: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    var showTagDialog by remember { mutableStateOf(false) }
    var showThemePickerSheet by remember { mutableStateOf(false) }

    val progress = if (uiState.totalSeconds > 0) {
        1f - (uiState.secondsRemaining.toFloat() / uiState.totalSeconds)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "timerProgress"
    )

    val minutes = uiState.secondsRemaining / 60
    val seconds = uiState.secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val modeColor by animateColorAsState(
        targetValue = when (uiState.currentMode) {
            PomodoroMode.FOCUS -> CoralPrimary
            PomodoroMode.SHORT_BREAK -> MintCyan
            PomodoroMode.LONG_BREAK -> AmberGold
        },
        label = "modeColor"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        val isCompactHeight = maxHeight < 650.dp

        // If Full Screen Art Mode is active, render full-bleed canvas in background
        if (uiState.fullScreenArtMode) {
            ArtCanvas(
                theme = uiState.activeArtTheme,
                progress = animatedProgress,
                modifier = Modifier.fillMaxSize(),
                contentAlpha = 0.85f
            )
            // Dark overlay gradient for contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DarkBackground.copy(alpha = 0.5f),
                                DarkBackground.copy(alpha = 0.2f),
                                DarkBackground.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }

        if (isWideScreen) {
            // Adaptive 2-Pane Tablet / Landscape Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Art Canvas Showcase
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.88f)
                        .clip(RoundedCornerShape(24.dp))
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .testTag("session_art_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ArtCanvas(
                            theme = uiState.activeArtTheme,
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Controls Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkBackground.copy(alpha = 0.75f),
                                modifier = Modifier.clickable { showThemePickerSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Art theme",
                                        tint = modeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = uiState.activeArtTheme.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleFullScreen,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(DarkBackground.copy(alpha = 0.75f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen art mode",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Bottom Mantra Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, DarkBackground.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "✨ \"${uiState.activeArtTheme.defaultMantra}\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary.copy(alpha = 0.95f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Right Column: Controls & Timer Ring
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Mode Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PomodoroMode.values().forEach { mode ->
                            val isSelected = uiState.currentMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSwitchMode(mode) },
                                label = {
                                    Text(
                                        text = mode.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = modeColor,
                                    containerColor = DarkSurfaceVariant.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .testTag("mode_chip_${mode.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timer Circular Ring displaying remaining time
                    CircularTimerProgressIndicator(
                        secondsRemaining = uiState.secondsRemaining,
                        totalSeconds = uiState.totalSeconds,
                        currentMode = uiState.currentMode,
                        isRunning = uiState.isRunning,
                        isPaused = uiState.isPaused,
                        modeColor = modeColor,
                        dialSize = 190.dp,
                        strokeWidth = 9.dp,
                        onClick = {
                            if (uiState.isRunning) onPause() else onStart()
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                                .testTag("reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset timer",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Button(
                            onClick = { if (uiState.isRunning) onPause() else onStart() },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                            modifier = Modifier
                                .height(52.dp)
                                .width(140.dp)
                                .shadow(8.dp, RoundedCornerShape(24.dp))
                                .testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause" else "Start",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isRunning) "PAUSE" else "START",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                val next = uiState.currentMode.getNextMode(uiState.completedRounds + 1)
                                onSwitchMode(next)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                                .testTag("skip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Skip to next mode",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Adaptive Portrait Phone Layout - Proportionally sized to fit screen without overflowing
            val artCardHeight = if (isCompactHeight) 120.dp else 155.dp
            val timerRingSize = if (isCompactHeight) 160.dp else 185.dp
            val spacerGap = if (isCompactHeight) 6.dp else 10.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacerGap)
            ) {
                // Top Section: Mode Switcher Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PomodoroMode.values().forEach { mode ->
                        val isSelected = uiState.currentMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSwitchMode(mode) },
                            label = {
                                Text(
                                    text = mode.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = modeColor,
                                containerColor = DarkSurfaceVariant.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) modeColor else Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .testTag("mode_chip_${mode.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacerGap))

                // Middle Section 1: Active Session Art Card
                if (!uiState.fullScreenArtMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(artCardHeight)
                            .clip(RoundedCornerShape(20.dp))
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .testTag("session_art_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ArtCanvas(
                                theme = uiState.activeArtTheme,
                                progress = animatedProgress,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top Controls Overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkBackground.copy(alpha = 0.75f),
                                    modifier = Modifier.clickable { showThemePickerSheet = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Art theme",
                                            tint = modeColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = uiState.activeArtTheme.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onToggleFullScreen,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(DarkBackground.copy(alpha = 0.75f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen art mode",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Bottom Mantra Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, DarkBackground.copy(alpha = 0.85f))
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✨ \"${uiState.activeArtTheme.defaultMantra}\"",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary.copy(alpha = 0.95f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // In Fullscreen mode, show compact bar with art title and exit fullscreen button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.clickable { showThemePickerSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = modeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.activeArtTheme.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleFullScreen,
                            modifier = Modifier
                                .size(32.dp)
                                .background(DarkSurfaceVariant.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit fullscreen",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacerGap))

                // Middle Section 2: Task Tag & Round Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier.clickable { showTagDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = "Task tag",
                                tint = modeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.currentTaskTag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit tag",
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Rounds",
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Round ${(uiState.completedRounds % 4) + 1} / 4",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacerGap))

                // Middle Section 3: Circular Progress Indicator displaying remaining time
                CircularTimerProgressIndicator(
                    secondsRemaining = uiState.secondsRemaining,
                    totalSeconds = uiState.totalSeconds,
                    currentMode = uiState.currentMode,
                    isRunning = uiState.isRunning,
                    isPaused = uiState.isPaused,
                    modeColor = modeColor,
                    dialSize = timerRingSize,
                    strokeWidth = if (isCompactHeight) 8.dp else 9.dp,
                    onClick = {
                        if (uiState.isRunning) onPause() else onStart()
                    }
                )

                Spacer(modifier = Modifier.height(spacerGap))

                // Bottom Section 1: Quick Time Adjustments
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAddMinutes(-1) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.testTag("minus_1m_button")
                    ) {
                        Text("-1 min", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { onAddMinutes(5) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        modifier = Modifier.testTag("plus_5m_button")
                    ) {
                        Text("+5 min", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(spacerGap))

                // Bottom Section 2: Main Action Controls (Play/Pause, Reset, Skip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .testTag("reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset timer",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    Button(
                        onClick = {
                            if (uiState.isRunning) onPause() else onStart()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = modeColor),
                        modifier = Modifier
                            .height(52.dp)
                            .width(140.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .testTag("play_pause_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause" else "Start",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isRunning) "PAUSE" else "START",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    IconButton(
                        onClick = {
                            val next = uiState.currentMode.getNextMode(uiState.completedRounds + 1)
                            onSwitchMode(next)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                            .testTag("skip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip to next mode",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Task Tag Edit Dialog
    if (showTagDialog) {
        var newTag by remember { mutableStateOf(uiState.currentTaskTag) }
        val quickTags = listOf("Deep Work", "Coding", "Study & Learn", "Design", "Writing", "Research", "Reading", "Planning")

        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Set Task Objective", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("Task / Goal Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Popular Tags:", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickTags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier.clickable { newTag = tag }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTag.isNotBlank()) onSetTaskTag(newTag.trim())
                        showTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Quick Theme Picker Dialog
    if (showThemePickerSheet) {
        AlertDialog(
            onDismissRequest = { showThemePickerSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Choose Session Art", color = TextPrimary, fontSize = 18.sp)
                    TextButton(onClick = {
                        showThemePickerSheet = false
                        onNavigateToGallery()
                    }) {
                        Text("Art Studio →", color = CoralPrimary, fontSize = 13.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    availableThemes.forEach { theme ->
                        val isCurrent = uiState.activeArtTheme.id == theme.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSelectArtTheme(theme)
                                    showThemePickerSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) DarkSurfaceElevated else DarkSurfaceVariant
                            ),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, CoralPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    ArtCanvas(theme = theme, interactive = false)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = theme.subtitle,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePickerSheet = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Session Completed Celebration Dialog
    if (uiState.showSessionCompleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissCompleteDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉 Session Completed!", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        ArtCanvas(
                            theme = uiState.activeArtTheme,
                            progress = 1f,
                            interactive = true
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Great work! You logged ${uiState.lastCompletedSessionDuration} minutes on \"${uiState.currentTaskTag}\".",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Take a well-deserved break to refresh your mind.",
                        color = AmberGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCompleteDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Start Break")
                }
            },
            containerColor = DarkSurface
        )
    }
}
