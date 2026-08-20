package com.example.ui.art

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettingsEntity
import com.example.model.ArtTheme
import com.example.model.PomodoroMode
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtGalleryScreen(
    allThemes: List<ArtTheme>,
    currentSettings: UserSettingsEntity,
    onSelectForMode: (themeId: String, mode: PomodoroMode) -> Unit,
    onSaveCustomTheme: (ArtTheme) -> Unit,
    onDeleteCustomTheme: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var themeToEdit by remember { mutableStateOf<ArtTheme?>(null) }
    var showCustomizerDialog by remember { mutableStateOf(false) }
    var fullScreenPreviewTheme by remember { mutableStateOf<ArtTheme?>(null) }

    val categories = remember(allThemes) {
        listOf("All") + allThemes.map { it.category }.distinct()
    }

    val filteredThemes = remember(allThemes, selectedCategory) {
        if (selectedCategory == "All") allThemes
        else allThemes.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle & Explanation
            Text(
                text = "Personalize your focus atmosphere. Art themes inspire immersion during deep work and breaks.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPrimary,
                            containerColor = DarkSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Themes List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredThemes, key = { it.id }) { theme ->
                    ArtThemeCard(
                        theme = theme,
                        isFocusTheme = currentSettings.focusArtThemeId == theme.id,
                        isShortBreakTheme = currentSettings.shortBreakArtThemeId == theme.id,
                        isLongBreakTheme = currentSettings.longBreakArtThemeId == theme.id,
                        onSetForMode = { mode -> onSelectForMode(theme.id, mode) },
                        onPreview = { fullScreenPreviewTheme = theme },
                        onEdit = {
                            themeToEdit = theme
                            showCustomizerDialog = true
                        },
                        onDelete = { onDeleteCustomTheme(theme.id) }
                    )
                }
            }
        }

        // Floating Action Button: Create Custom Art
        FloatingActionButton(
            onClick = {
                themeToEdit = null
                showCustomizerDialog = true
            },
            containerColor = CoralPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_art_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Custom Art")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Art", fontWeight = FontWeight.Bold)
            }
        }

        // Fullscreen Live Preview Modal
        if (fullScreenPreviewTheme != null) {
            val preview = fullScreenPreviewTheme!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ArtCanvas(theme = preview, interactive = true, modifier = Modifier.fillMaxSize())

                // Overlay Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkBackground.copy(alpha = 0.8f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(preview.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(preview.defaultMantra, fontSize = 12.sp, color = AmberGold)
                        }
                    }

                    IconButton(
                        onClick = { fullScreenPreviewTheme = null },
                        modifier = Modifier
                            .size(44.dp)
                            .background(DarkBackground.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close preview", tint = Color.White)
                    }
                }
            }
        }
    }

    if (showCustomizerDialog) {
        ArtCustomizerDialog(
            initialTheme = themeToEdit,
            onDismiss = { showCustomizerDialog = false },
            onSaveTheme = { saved ->
                onSaveCustomTheme(saved)
                showCustomizerDialog = false
            }
        )
    }
}

@Composable
private fun ArtThemeCard(
    theme: ArtTheme,
    isFocusTheme: Boolean,
    isShortBreakTheme: Boolean,
    isLongBreakTheme: Boolean,
    onSetForMode: (PomodoroMode) -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .testTag("art_card_${theme.id}")
    ) {
        Column {
            // Live Interactive Preview Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                ArtCanvas(
                    theme = theme,
                    interactive = true,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Badges (Category & Active Mode Pills)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBackground.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = theme.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isFocusTheme) {
                            ActivePill("Focus", CoralPrimary)
                        }
                        if (isShortBreakTheme) {
                            ActivePill("Short Break", MintCyan)
                        }
                        if (isLongBreakTheme) {
                            ActivePill("Long Break", AmberGold)
                        }
                    }
                }

                // Fullscreen preview action in bottom right of canvas
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .background(DarkBackground.copy(alpha = 0.75f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen preview",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Body Info & Mode Assignment Actions
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = theme.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = theme.subtitle,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    if (theme.isCustom) {
                        Row {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit theme", tint = TextSecondary)
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete theme", tint = CoralPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Motivational Mantra quote box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkBackground.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✨ \"${theme.defaultMantra}\"",
                        fontSize = 12.sp,
                        color = AmberGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Assignment Action Buttons
                Text("Set as session art for:", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSetForMode(PomodoroMode.FOCUS) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isFocusTheme) CoralPrimary else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFocusTheme) CoralPrimary else DarkSurface
                        )
                    ) {
                        Text("Focus", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { onSetForMode(PomodoroMode.SHORT_BREAK) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isShortBreakTheme) MintCyan else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isShortBreakTheme) MintCyan else DarkSurface
                        )
                    ) {
                        Text("Short Break", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { onSetForMode(PomodoroMode.LONG_BREAK) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isLongBreakTheme) AmberGold else TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLongBreakTheme) AmberGold else DarkSurface
                        )
                    ) {
                        Text("Long Break", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivePill(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
