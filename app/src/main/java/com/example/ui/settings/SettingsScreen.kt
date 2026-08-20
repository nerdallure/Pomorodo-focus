package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettingsEntity
import com.example.model.ArtTheme
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: UserSettingsEntity,
    allThemes: List<ArtTheme>,
    onUpdateSettings: (UserSettingsEntity) -> Unit,
    onNavigateToGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Timer Durations Section
        SectionHeader(icon = Icons.Default.AccessTime, title = "Session Durations")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Focus Duration Slider
                DurationSliderRow(
                    title = "Focus Session",
                    minutes = settings.focusMinutes,
                    min = 5,
                    max = 90,
                    accentColor = CoralPrimary,
                    onValueChange = { onUpdateSettings(settings.copy(focusMinutes = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Short Break Slider
                DurationSliderRow(
                    title = "Short Break",
                    minutes = settings.shortBreakMinutes,
                    min = 1,
                    max = 20,
                    accentColor = MintCyan,
                    onValueChange = { onUpdateSettings(settings.copy(shortBreakMinutes = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Long Break Slider
                DurationSliderRow(
                    title = "Long Break",
                    minutes = settings.longBreakMinutes,
                    min = 5,
                    max = 45,
                    accentColor = AmberGold,
                    onValueChange = { onUpdateSettings(settings.copy(longBreakMinutes = it)) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Long Break Interval
                DurationSliderRow(
                    title = "Rounds before Long Break",
                    minutes = settings.roundsBeforeLongBreak,
                    min = 2,
                    max = 8,
                    unitLabel = "rounds",
                    accentColor = CoralPrimary,
                    onValueChange = { onUpdateSettings(settings.copy(roundsBeforeLongBreak = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Automation Section
        SectionHeader(icon = Icons.Default.Repeat, title = "Automation")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ToggleRow(
                    title = "Auto-start Breaks",
                    subtitle = "Automatically begin break timer when focus completes",
                    checked = settings.autoStartBreaks,
                    onCheckedChange = { onUpdateSettings(settings.copy(autoStartBreaks = it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "Auto-start Focus",
                    subtitle = "Automatically begin next focus round when break ends",
                    checked = settings.autoStartFocus,
                    onCheckedChange = { onUpdateSettings(settings.copy(autoStartFocus = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Audio & Haptics Section
        SectionHeader(icon = Icons.Default.VolumeUp, title = "Audio & Ambient Sound")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ToggleRow(
                    title = "Harmonic Chime on Completion",
                    subtitle = "Play peaceful harmonic bell tone when session ends",
                    checked = settings.soundEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "Clock Ticking Sound",
                    subtitle = "Subtle soft mechanical tick during focus countdown",
                    checked = settings.tickingEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(tickingEnabled = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ambient Focus Noise
                Text("Ambient Focus Generator", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Plays relaxing synthesized background sound during focus", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                val ambientOptions = listOf(
                    Pair("NONE", "Off"),
                    Pair("RAIN", "Raindrops"),
                    Pair("ZEN_BELLS", "Zen 432Hz"),
                    Pair("WHITE_NOISE", "Pink Noise")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ambientOptions.forEach { (key, label) ->
                        val isSelected = settings.ambientSound == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onUpdateSettings(settings.copy(ambientSound = key)) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                containerColor = DarkSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Assigned Art Themes Overview
        SectionHeader(icon = Icons.Default.AutoAwesome, title = "Session Art Defaults")

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val focusTheme = ArtTheme.getById(settings.focusArtThemeId, allThemes)
                val shortBreakTheme = ArtTheme.getById(settings.shortBreakArtThemeId, allThemes)
                val longBreakTheme = ArtTheme.getById(settings.longBreakArtThemeId, allThemes)

                ArtSummaryRow("Focus Mode Art", focusTheme.name, CoralPrimary, onClick = onNavigateToGallery)
                Spacer(modifier = Modifier.height(10.dp))
                ArtSummaryRow("Short Break Art", shortBreakTheme.name, MintCyan, onClick = onNavigateToGallery)
                Spacer(modifier = Modifier.height(10.dp))
                ArtSummaryRow("Long Break Art", longBreakTheme.name, AmberGold, onClick = onNavigateToGallery)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
    }
}

@Composable
private fun DurationSliderRow(
    title: String,
    minutes: Int,
    min: Int,
    max: Int,
    unitLabel: String = "min",
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(
                text = "$minutes $unitLabel",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Slider(
            value = minutes.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = DarkSurface
            )
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CoralPrimary,
                uncheckedTrackColor = DarkSurface
            )
        )
    }
}

@Composable
private fun ArtSummaryRow(
    modeLabel: String,
    artName: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(modeLabel, fontSize = 11.sp, color = TextSecondary)
                Text(artName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Text("Change →", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}
