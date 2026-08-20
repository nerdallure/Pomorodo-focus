package com.example.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.AppDatabase
import com.example.data.PomodoroRepository
import com.example.data.SessionEntity
import com.example.data.UserSettingsEntity
import com.example.model.ArtTheme
import com.example.model.PomodoroMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class TimerUiState(
    val currentMode: PomodoroMode = PomodoroMode.FOCUS,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val completedRounds: Int = 0,
    val currentTaskTag: String = "Deep Work",
    val activeArtTheme: ArtTheme = ArtTheme.DefaultFocusTheme,
    val fullScreenArtMode: Boolean = false,
    val customMantra: String = "",
    val showSessionCompleteDialog: Boolean = false,
    val lastCompletedSessionDuration: Int = 0
)

data class StreakStats(
    val todayFocusMinutes: Int = 0,
    val todayCompletedPomodoros: Int = 0,
    val currentStreakDays: Int = 0,
    val totalFocusMinutes: Int = 0,
    val weeklyMinutes: List<Pair<String, Int>> = emptyList()
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PomodoroRepository = PomodoroRepository(
        AppDatabase.getInstance(application).pomodoroDao()
    )
    val soundManager: SoundManager = SoundManager(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    val allCustomThemes: StateFlow<List<ArtTheme>> = repository.customArtThemes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    val sessionHistory: StateFlow<List<SessionEntity>> = repository.completedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined all available themes (Built-in + Custom)
    val allThemes: StateFlow<List<ArtTheme>> = allCustomThemes.map { customList ->
        ArtTheme.BuiltInThemes + customList
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ArtTheme.BuiltInThemes
    )

    // Calculated Streak & Analytics
    val stats: StateFlow<StreakStats> = repository.completedSessions.mapToStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakStats())

    init {
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                val themes = allThemes.value
                val activeTheme = when (_uiState.value.currentMode) {
                    PomodoroMode.FOCUS -> ArtTheme.getById(settings.focusArtThemeId, themes)
                    PomodoroMode.SHORT_BREAK -> ArtTheme.getById(settings.shortBreakArtThemeId, themes)
                    PomodoroMode.LONG_BREAK -> ArtTheme.getById(settings.longBreakArtThemeId, themes)
                }

                if (!_uiState.value.isRunning && !_uiState.value.isPaused) {
                    val durationMins = when (_uiState.value.currentMode) {
                        PomodoroMode.FOCUS -> settings.focusMinutes
                        PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes
                        PomodoroMode.LONG_BREAK -> settings.longBreakMinutes
                    }
                    _uiState.update {
                        it.copy(
                            totalSeconds = durationMins * 60,
                            secondsRemaining = durationMins * 60,
                            currentTaskTag = settings.currentTaskTag,
                            activeArtTheme = activeTheme,
                            fullScreenArtMode = settings.fullScreenArtMode
                        )
                    }
                }
            }
        }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return

        timerJob?.cancel()

        soundManager.playClickSound()
        soundManager.vibrateClick()

        // Start ambient sound if enabled in settings
        val ambientType = userSettings.value.ambientSound
        if (ambientType != "NONE" && _uiState.value.currentMode == PomodoroMode.FOCUS) {
            soundManager.startAmbient(ambientType)
        }

        // If secondsRemaining is 0 or less, refresh to totalSeconds
        if (_uiState.value.secondsRemaining <= 0) {
            val defaultDuration = _uiState.value.totalSeconds.takeIf { it > 0 } ?: (25 * 60)
            _uiState.update { it.copy(secondsRemaining = defaultDuration, totalSeconds = defaultDuration) }
        }

        _uiState.update { it.copy(isRunning = true, isPaused = false) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0) {
                delay(1000)
                if (userSettings.value.tickingEnabled && _uiState.value.currentMode == PomodoroMode.FOCUS) {
                    soundManager.playTickSound()
                }
                _uiState.update {
                    val nextRemaining = (it.secondsRemaining - 1).coerceAtLeast(0)
                    it.copy(secondsRemaining = nextRemaining)
                }
            }
            onSessionFinished()
        }
    }

    fun pauseTimer() {
        soundManager.playClickSound()
        soundManager.vibrateClick()
        soundManager.stopAmbient()
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isRunning = false, isPaused = true) }
    }

    fun resetTimer() {
        soundManager.playClickSound()
        soundManager.stopAmbient()
        timerJob?.cancel()
        timerJob = null

        val settings = userSettings.value
        val durationMins = when (_uiState.value.currentMode) {
            PomodoroMode.FOCUS -> settings.focusMinutes
            PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes
            PomodoroMode.LONG_BREAK -> settings.longBreakMinutes
        }

        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                totalSeconds = durationMins * 60,
                secondsRemaining = durationMins * 60
            )
        }
    }

    fun switchMode(mode: PomodoroMode) {
        soundManager.playClickSound()
        soundManager.stopAmbient()
        timerJob?.cancel()
        timerJob = null

        val settings = userSettings.value
        val durationMins = when (mode) {
            PomodoroMode.FOCUS -> settings.focusMinutes
            PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes
            PomodoroMode.LONG_BREAK -> settings.longBreakMinutes
        }

        val themeId = when (mode) {
            PomodoroMode.FOCUS -> settings.focusArtThemeId
            PomodoroMode.SHORT_BREAK -> settings.shortBreakArtThemeId
            PomodoroMode.LONG_BREAK -> settings.longBreakArtThemeId
        }
        val theme = ArtTheme.getById(themeId, allThemes.value)

        _uiState.update {
            it.copy(
                currentMode = mode,
                isRunning = false,
                isPaused = false,
                totalSeconds = durationMins * 60,
                secondsRemaining = durationMins * 60,
                activeArtTheme = theme
            )
        }
    }

    fun addMinutes(minutes: Int) {
        soundManager.vibrateClick()
        _uiState.update {
            val newTotal = (it.totalSeconds + minutes * 60).coerceAtLeast(60)
            val newRemaining = (it.secondsRemaining + minutes * 60).coerceIn(1, newTotal)
            it.copy(totalSeconds = newTotal, secondsRemaining = newRemaining)
        }
    }

    fun setTaskTag(tag: String) {
        _uiState.update { it.copy(currentTaskTag = tag) }
        viewModelScope.launch {
            repository.updateSettings(userSettings.value.copy(currentTaskTag = tag))
        }
    }

    fun selectArtTheme(theme: ArtTheme) {
        soundManager.vibrateClick()
        _uiState.update { it.copy(activeArtTheme = theme) }

        // Also save preference for current mode
        viewModelScope.launch {
            val currentSettings = userSettings.value
            val updated = when (_uiState.value.currentMode) {
                PomodoroMode.FOCUS -> currentSettings.copy(focusArtThemeId = theme.id)
                PomodoroMode.SHORT_BREAK -> currentSettings.copy(shortBreakArtThemeId = theme.id)
                PomodoroMode.LONG_BREAK -> currentSettings.copy(longBreakArtThemeId = theme.id)
            }
            repository.updateSettings(updated)
        }
    }

    fun setArtThemeForMode(themeId: String, mode: PomodoroMode) {
        viewModelScope.launch {
            val currentSettings = userSettings.value
            val updated = when (mode) {
                PomodoroMode.FOCUS -> currentSettings.copy(focusArtThemeId = themeId)
                PomodoroMode.SHORT_BREAK -> currentSettings.copy(shortBreakArtThemeId = themeId)
                PomodoroMode.LONG_BREAK -> currentSettings.copy(longBreakArtThemeId = themeId)
            }
            repository.updateSettings(updated)
            if (_uiState.value.currentMode == mode) {
                val theme = ArtTheme.getById(themeId, allThemes.value)
                _uiState.update { it.copy(activeArtTheme = theme) }
            }
        }
    }

    fun toggleFullScreenArt() {
        val next = !_uiState.value.fullScreenArtMode
        _uiState.update { it.copy(fullScreenArtMode = next) }
        viewModelScope.launch {
            repository.updateSettings(userSettings.value.copy(fullScreenArtMode = next))
        }
    }

    fun dismissSessionCompleteDialog() {
        _uiState.update { it.copy(showSessionCompleteDialog = false) }
    }

    private fun onSessionFinished() {
        soundManager.stopAmbient()
        if (userSettings.value.soundEnabled) {
            soundManager.playCompletionChime()
        }
        soundManager.vibrateSessionComplete()

        val finishedMode = _uiState.value.currentMode
        val durationMins = _uiState.value.totalSeconds / 60
        val artName = _uiState.value.activeArtTheme.name
        val artId = _uiState.value.activeArtTheme.id
        val taskTag = _uiState.value.currentTaskTag

        // Log session to Room database
        viewModelScope.launch {
            repository.logCompletedSession(
                mode = finishedMode.name,
                durationMinutes = durationMins,
                artThemeId = artId,
                artThemeName = artName,
                taskTag = taskTag
            )
        }

        val newCompletedRounds = if (finishedMode == PomodoroMode.FOCUS) {
            _uiState.value.completedRounds + 1
        } else {
            _uiState.value.completedRounds
        }

        val nextMode = finishedMode.getNextMode(
            completedRounds = newCompletedRounds,
            roundsBeforeLongBreak = userSettings.value.roundsBeforeLongBreak
        )

        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                completedRounds = newCompletedRounds,
                showSessionCompleteDialog = true,
                lastCompletedSessionDuration = durationMins
            )
        }

        // Switch to next mode
        val autoStart = if (finishedMode == PomodoroMode.FOCUS) {
            userSettings.value.autoStartBreaks
        } else {
            userSettings.value.autoStartFocus
        }

        switchMode(nextMode)

        if (autoStart) {
            startTimer()
        }
    }

    fun saveCustomTheme(theme: ArtTheme) {
        viewModelScope.launch {
            repository.saveCustomArtTheme(theme)
            selectArtTheme(theme)
        }
    }

    fun deleteCustomTheme(id: String) {
        viewModelScope.launch {
            repository.deleteCustomArtTheme(id)
            if (_uiState.value.activeArtTheme.id == id) {
                selectArtTheme(ArtTheme.DefaultFocusTheme)
            }
        }
    }

    fun updateSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            // Adjust current duration if timer not running
            if (!_uiState.value.isRunning && !_uiState.value.isPaused) {
                val durationMins = when (_uiState.value.currentMode) {
                    PomodoroMode.FOCUS -> settings.focusMinutes
                    PomodoroMode.SHORT_BREAK -> settings.shortBreakMinutes
                    PomodoroMode.LONG_BREAK -> settings.longBreakMinutes
                }
                _uiState.update {
                    it.copy(
                        totalSeconds = durationMins * 60,
                        secondsRemaining = durationMins * 60
                    )
                }
            }
        }
    }

    fun deleteSessionRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}

private fun Flow<List<SessionEntity>>.mapToStats(): Flow<StreakStats> {
    return this.map { sessions: List<SessionEntity> ->
        val focusSessions: List<SessionEntity> = sessions.filter { it.mode == PomodoroMode.FOCUS.name && it.completed }
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDay = now.get(Calendar.DAY_OF_YEAR)

        var todayMinutes = 0
        var todayCount = 0
        var totalMinutes = 0

        // Distinct days set for streak calculation
        val activeDaysSet = mutableSetOf<Pair<Int, Int>>()

        // Weekly 7 days buckets
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weeklyMinutesMap = IntArray(7) { 0 }

        for (s in focusSessions) {
            totalMinutes += s.durationMinutes
            val cal = Calendar.getInstance().apply { timeInMillis = s.timestamp }
            val y = cal.get(Calendar.YEAR)
            val d = cal.get(Calendar.DAY_OF_YEAR)

            if (y == todayYear && d == todayDay) {
                todayMinutes += s.durationMinutes
                todayCount += 1
            }

            activeDaysSet.add(Pair(y, d))

            // Check if within last 7 days
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday, ...
            val mappedIdx = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            weeklyMinutesMap[mappedIdx] += s.durationMinutes
        }

        // Streak calculation
        var streak = 0
        val checkCal = Calendar.getInstance()
        while (true) {
            val y = checkCal.get(Calendar.YEAR)
            val d = checkCal.get(Calendar.DAY_OF_YEAR)
            if (activeDaysSet.contains(Pair(y, d))) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // If today has no sessions yet, check if yesterday was active
                if (streak == 0 && (y == todayYear && d == todayDay)) {
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    val yPrev = checkCal.get(Calendar.YEAR)
                    val dPrev = checkCal.get(Calendar.DAY_OF_YEAR)
                    if (activeDaysSet.contains(Pair(yPrev, dPrev))) {
                        streak++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }

        val weeklyList = dayNames.mapIndexed { idx, name ->
            Pair(name, weeklyMinutesMap[idx])
        }

        StreakStats(
            todayFocusMinutes = todayMinutes,
            todayCompletedPomodoros = todayCount,
            currentStreakDays = streak,
            totalFocusMinutes = totalMinutes,
            weeklyMinutes = weeklyList
        )
    }
}
