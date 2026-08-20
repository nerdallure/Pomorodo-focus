package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.SessionEntity
import com.example.data.UserSettingsEntity
import com.example.model.ArtTheme
import com.example.model.PomodoroMode
import com.example.ui.art.ArtCanvas
import com.example.ui.art.ArtGalleryScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.timer.StreakStats
import com.example.ui.timer.TimerScreen
import com.example.ui.timer.TimerUiState
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun capture_store_01_focus_timer() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          TimerScreen(
            uiState = TimerUiState(
              currentMode = PomodoroMode.FOCUS,
              secondsRemaining = 24 * 60 + 38,
              totalSeconds = 25 * 60,
              isRunning = true,
              isPaused = false,
              completedRounds = 3,
              activeArtTheme = ArtTheme.DefaultFocusTheme,
              currentTaskTag = "Deep Coding",
              customMantra = "Focus on what matters most."
            ),
            availableThemes = ArtTheme.BuiltInThemes,
            onStart = {},
            onPause = {},
            onReset = {},
            onSwitchMode = {},
            onAddMinutes = {},
            onSelectArtTheme = {},
            onSetTaskTag = {},
            onToggleFullScreen = {},
            onDismissCompleteDialog = {},
            onNavigateToGallery = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/01_focus_timer.png")
  }

  @Test
  fun capture_store_02_art_gallery() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          ArtGalleryScreen(
            allThemes = ArtTheme.BuiltInThemes,
            currentSettings = UserSettingsEntity(),
            onSelectForMode = { _, _ -> },
            onSaveCustomTheme = {},
            onDeleteCustomTheme = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/02_art_gallery.png")
  }

  @Test
  fun capture_store_03_stats_analytics() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          StatsScreen(
            stats = StreakStats(
              todayFocusMinutes = 125,
              todayCompletedPomodoros = 5,
              currentStreakDays = 7,
              totalFocusMinutes = 1420,
              weeklyMinutes = listOf(
                "Mon" to 95,
                "Tue" to 120,
                "Wed" to 110,
                "Thu" to 125,
                "Fri" to 80,
                "Sat" to 45,
                "Sun" to 60
              )
            ),
            sessionHistory = listOf(
              SessionEntity(
                id = 1,
                mode = "FOCUS",
                durationMinutes = 25,
                artThemeId = "cherry_blossom",
                artThemeName = "Cherry Blossom Zen",
                taskTag = "Deep Coding",
                timestamp = System.currentTimeMillis() - 1800000
              ),
              SessionEntity(
                id = 2,
                mode = "SHORT_BREAK",
                durationMinutes = 5,
                artThemeId = "bamboo_mist",
                artThemeName = "Bamboo Mist",
                taskTag = "Rest & Breathe",
                timestamp = System.currentTimeMillis() - 3600000
              ),
              SessionEntity(
                id = 3,
                mode = "FOCUS",
                durationMinutes = 25,
                artThemeId = "celestial_aurora",
                artThemeName = "Celestial Aurora",
                taskTag = "Product Design",
                timestamp = System.currentTimeMillis() - 7200000
              )
            ),
            onDeleteSession = {},
            onClearAllHistory = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/03_stats_analytics.png")
  }

  @Test
  fun capture_store_04_ambient_settings() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          com.example.ui.settings.SettingsScreen(
            settings = UserSettingsEntity(
              focusMinutes = 25,
              shortBreakMinutes = 5,
              longBreakMinutes = 15,
              roundsBeforeLongBreak = 4,
              ambientSound = "RAIN",
              soundEnabled = true,
              tickingEnabled = true
            ),
            allThemes = ArtTheme.BuiltInThemes,
            onUpdateSettings = {},
            onNavigateToGallery = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/04_settings_soundscapes.png")
  }

  @Test
  fun capture_timer_screen() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          TimerScreen(
            uiState = TimerUiState(
              currentMode = PomodoroMode.FOCUS,
              secondsRemaining = 25 * 60,
              totalSeconds = 25 * 60,
              isRunning = false,
              isPaused = false,
              completedRounds = 1,
              activeArtTheme = ArtTheme.DefaultFocusTheme,
              currentTaskTag = "Deep Work",
              customMantra = "Focus on the journey, one petal at a time."
            ),
            availableThemes = ArtTheme.BuiltInThemes,
            onStart = {},
            onPause = {},
            onReset = {},
            onSwitchMode = {},
            onAddMinutes = {},
            onSelectArtTheme = {},
            onSetTaskTag = {},
            onToggleFullScreen = {},
            onDismissCompleteDialog = {},
            onNavigateToGallery = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/timer_screen.png")
  }

  @Test
  fun capture_art_gallery_screen() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          ArtGalleryScreen(
            allThemes = ArtTheme.BuiltInThemes,
            currentSettings = UserSettingsEntity(),
            onSelectForMode = { _, _ -> },
            onSaveCustomTheme = {},
            onDeleteCustomTheme = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/art_gallery_screen.png")
  }

  @Test
  fun capture_stats_screen() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
          StatsScreen(
            stats = StreakStats(
              todayFocusMinutes = 75,
              todayCompletedPomodoros = 3,
              currentStreakDays = 5,
              totalFocusMinutes = 240,
              weeklyMinutes = listOf(
                "Mon" to 45,
                "Tue" to 60,
                "Wed" to 50,
                "Thu" to 75,
                "Fri" to 0,
                "Sat" to 0,
                "Sun" to 0
              )
            ),
            sessionHistory = listOf(
              SessionEntity(
                id = 1,
                mode = "FOCUS",
                durationMinutes = 25,
                artThemeId = "cherry_blossom",
                artThemeName = "Cherry Blossom Zen",
                taskTag = "Deep Work",
                timestamp = System.currentTimeMillis() - 3600000
              ),
              SessionEntity(
                id = 2,
                mode = "SHORT_BREAK",
                durationMinutes = 5,
                artThemeId = "bamboo_mist",
                artThemeName = "Bamboo Mist",
                taskTag = "Deep Work",
                timestamp = System.currentTimeMillis() - 1800000
              ),
              SessionEntity(
                id = 3,
                mode = "FOCUS",
                durationMinutes = 25,
                artThemeId = "celestial_aurora",
                artThemeName = "Celestial Aurora",
                taskTag = "App Design",
                timestamp = System.currentTimeMillis() - 900000
              )
            ),
            onDeleteSession = {},
            onClearAllHistory = {}
          )
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/stats_screen.png")
  }

  @Test
  fun capture_artwork_showcase() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(modifier = Modifier.size(400.dp, 400.dp)) {
          ArtCanvas(theme = ArtTheme.DefaultFocusTheme, interactive = false)
        }
      }
    }
    composeTestRule.mainClock.advanceTimeBy(500)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/artwork_showcase.png")
  }
}

