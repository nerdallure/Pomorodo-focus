package com.example.ui.art

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArtTheme
import com.example.model.MotifType
import com.example.model.ParticleEffectType
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

private data class ColorPalettePreset(
    val name: String,
    val colors: List<Long>,
    val moodColor: Long
)

private val PalettePresets = listOf(
    ColorPalettePreset("Twilight Violet", listOf(0xFF1E152A, 0xFF2E1C2B, 0xFF422835), 0xFFFFB86C),
    ColorPalettePreset("Emerald Forest", listOf(0xFF081C15, 0xFF1B4332, 0xFF2D6A4F), 0xFF52B788),
    ColorPalettePreset("Cyber Magenta", listOf(0xFF0D0221, 0xFF240046, 0xFF3C096C), 0xFF00F0FF),
    ColorPalettePreset("Cosmic Midnight", listOf(0xFF050510, 0xFF120E2E, 0xFF251A4A), 0xFFBD93F9),
    ColorPalettePreset("Golden Sunset", listOf(0xFF331800, 0xFF662D00, 0xFF994400), 0xFFFF9E00),
    ColorPalettePreset("Sakura Pink", listOf(0xFF2B1028, 0xFF4A1942, 0xFF6B2D5C), 0xFFFF79C6),
    ColorPalettePreset("Zen Mist", listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364), 0xFF81C784),
    ColorPalettePreset("Warm Hearth", listOf(0xFF1F0D06, 0xFF381A0C, 0xFF542914), 0xFFFF7043),
    ColorPalettePreset("Minimal Ink", listOf(0xFF141416, 0xFF1E1E24, 0xFF2A2A35), 0xFFFF5555)
)

@Composable
fun ArtCustomizerDialog(
    initialTheme: ArtTheme? = null,
    onDismiss: () -> Unit,
    onSaveTheme: (ArtTheme) -> Unit
) {
    var name by remember { mutableStateOf(initialTheme?.name ?: "My Focus Haven") }
    var subtitle by remember { mutableStateOf(initialTheme?.subtitle ?: "Custom meditative session art") }
    var selectedPalette by remember {
        mutableStateOf(
            PalettePresets.find { it.colors == initialTheme?.gradientColors } ?: PalettePresets[0]
        )
    }
    var selectedMotif by remember { mutableStateOf(initialTheme?.motifType ?: MotifType.ZEN_ENSO) }
    var selectedParticle by remember { mutableStateOf(initialTheme?.particleEffect ?: ParticleEffectType.FLOATING_SPARKS) }
    var mantra by remember { mutableStateOf(initialTheme?.defaultMantra ?: "Stay present. Flow in tranquility.") }

    val previewTheme = remember(name, subtitle, selectedPalette, selectedMotif, selectedParticle, mantra) {
        ArtTheme(
            id = initialTheme?.id ?: "custom_${UUID.randomUUID().toString().take(8)}",
            name = name.ifBlank { "Custom Artwork" },
            subtitle = subtitle.ifBlank { "Custom session theme" },
            category = "Custom",
            gradientColors = selectedPalette.colors,
            ambientMoodColor = selectedPalette.moodColor,
            motifType = selectedMotif,
            particleEffect = selectedParticle,
            defaultMantra = mantra.ifBlank { "Deep focus in progress" },
            isCustom = true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialTheme != null) "Edit Session Art" else "Compose New Art",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Live Interactive Preview Box
                Text("Live Art Preview (Tap to ripple)", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    ArtCanvas(theme = previewTheme, interactive = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artwork Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle Input
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Atmosphere / Subtitle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Palette Selector
                Text("Color Palette & Mood Aura", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PalettePresets.forEach { preset ->
                        val isSelected = selectedPalette.name == preset.name
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CoralPrimary) else null,
                            modifier = Modifier.clickable { selectedPalette = preset }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    preset.colors.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(Color(c), CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(preset.name, fontSize = 10.sp, color = if (isSelected) TextPrimary else TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Motif Element Selector
                Text("Visual Centerpiece (Motif)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MotifType.values().forEach { motif ->
                        val isSelected = selectedMotif == motif
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMotif = motif },
                            label = { Text(motif.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                containerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Particle Effect Selector
                Text("Ambient Animated Particles", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ParticleEffectType.values().forEach { effect ->
                        val isSelected = selectedParticle == effect
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedParticle = effect },
                            label = { Text(effect.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                containerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Motivation Mantra Input
                OutlinedTextField(
                    value = mantra,
                    onValueChange = { mantra = it },
                    label = { Text("Motivational Session Mantra") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveTheme(previewTheme)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Text("Save Artwork")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
