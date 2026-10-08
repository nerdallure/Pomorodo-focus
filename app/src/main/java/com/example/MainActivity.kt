package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.art.ArtGalleryScreen
import com.example.ui.components.BannerAdView
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.tasks.TasksScreen
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.timer.TimerScreen
import com.example.ui.timer.TimerViewModel
import com.google.android.gms.ads.MobileAds

enum class MainTab(val title: String, val icon: ImageVector, val tag: String) {
    TIMER("Timer", Icons.Default.Timer, "nav_timer"),
    TASKS("Tasks", Icons.Default.CheckCircle, "nav_tasks"),
    ART_STUDIO("Art Studio", Icons.Default.Palette, "nav_art"),
    STATS("Insights", Icons.Default.BarChart, "nav_stats"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        setContent {
            MyApplicationTheme(darkTheme = true) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val allThemes by viewModel.allThemes.collectAsStateWithLifecycle()
                val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
                val stats by viewModel.stats.collectAsStateWithLifecycle()
                val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
                val tasksWithSubtasks by viewModel.tasksWithSubtasks.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(MainTab.TIMER) }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    containerColor = DarkBackground,
                    contentColor = TextPrimary,
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = when (currentTab) {
                                        MainTab.TIMER -> "Pomodoro Art Focus"
                                        MainTab.TASKS -> "Tasks & Schedule"
                                        MainTab.ART_STUDIO -> "Session Art Studio"
                                        MainTab.STATS -> "Productivity & Insights"
                                        MainTab.SETTINGS -> "Preferences"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = if (uiState.fullScreenArtMode && currentTab == MainTab.TIMER) {
                                    Color.Transparent
                                } else {
                                    DarkBackground
                                }
                            )
                        )
                    },
                    bottomBar = {
                        if (!uiState.fullScreenArtMode) {
                            Column {
                                BannerAdView()
                                NavigationBar(
                                    containerColor = DarkSurface,
                                    contentColor = TextPrimary,
                                    modifier = Modifier.testTag("main_bottom_nav")
                                ) {
                                    MainTab.values().forEach { tab ->
                                        val isSelected = currentTab == tab
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                selectedTextColor = CoralPrimary,
                                                indicatorColor = CoralPrimary,
                                                unselectedIconColor = TextSecondary,
                                                unselectedTextColor = TextSecondary
                                            ),
                                            modifier = Modifier.testTag(tab.tag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabContent",
                            modifier = Modifier.fillMaxSize()
                        ) { tab ->
                            when (tab) {
                                MainTab.TIMER -> TimerScreen(
                                    uiState = uiState,
                                    availableThemes = allThemes,
                                    onStart = { viewModel.startTimer() },
                                    onPause = { viewModel.pauseTimer() },
                                    onReset = { viewModel.resetTimer() },
                                    onSwitchMode = { viewModel.switchMode(it) },
                                    onAddMinutes = { viewModel.addMinutes(it) },
                                    onSelectArtTheme = { viewModel.selectArtTheme(it) },
                                    onSetTaskTag = { viewModel.setTaskTag(it) },
                                    onToggleFullScreen = { viewModel.toggleFullScreenArt() },
                                    onDismissCompleteDialog = { viewModel.dismissSessionCompleteDialog() },
                                    onNavigateToGallery = { currentTab = MainTab.ART_STUDIO }
                                )

                                MainTab.TASKS -> TasksScreen(
                                    tasksWithSubtasks = tasksWithSubtasks,
                                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                                    onToggleSubtask = { viewModel.toggleSubtask(it) },
                                    onAddSubtask = { taskId, title -> viewModel.addSubtask(taskId, title) },
                                    onDeleteSubtask = { viewModel.deleteSubtask(it) },
                                    onDeleteTask = { viewModel.deleteTask(it) },
                                    onUpdateTask = { viewModel.updateTask(it) },
                                    onCreateTask = { title, desc, project, priority, dueDate, isRec, recRule, reminderTime, reminderStr, pomodoros, subtasks, isHabit ->
                                        viewModel.createTask(
                                            title, desc, project, priority, dueDate, isRec, recRule, reminderTime, reminderStr, pomodoros, subtasks, isHabit
                                        )
                                    },
                                    onFocusTask = { task ->
                                        viewModel.selectTaskForFocus(task)
                                        currentTab = MainTab.TIMER
                                    }
                                )

                                MainTab.ART_STUDIO -> ArtGalleryScreen(
                                    allThemes = allThemes,
                                    currentSettings = userSettings,
                                    onSelectForMode = { themeId, mode ->
                                        viewModel.setArtThemeForMode(themeId, mode)
                                    },
                                    onSaveCustomTheme = { viewModel.saveCustomTheme(it) },
                                    onDeleteCustomTheme = { viewModel.deleteCustomTheme(it) }
                                )

                                MainTab.STATS -> StatsScreen(
                                    stats = stats,
                                    sessionHistory = sessionHistory,
                                    onDeleteSession = { viewModel.deleteSessionRecord(it) },
                                    onClearAllHistory = { viewModel.clearAllHistory() }
                                )

                                MainTab.SETTINGS -> SettingsScreen(
                                    settings = userSettings,
                                    allThemes = allThemes,
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onNavigateToGallery = { currentTab = MainTab.ART_STUDIO }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
