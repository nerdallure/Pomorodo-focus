package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val durationMinutes: Int,
    val artThemeId: String,
    val artThemeName: String,
    val taskTag: String,
    val timestamp: Long = System.currentTimeMillis(),
    val completed: Boolean = true
)

@Entity(tableName = "custom_art_themes")
data class CustomArtThemeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val gradientColorsCsv: String,
    val ambientMoodColor: Long,
    val motifType: String,
    val particleEffect: String,
    val defaultMantra: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val roundsBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val tickingEnabled: Boolean = false,
    val ambientSound: String = "NONE",
    val focusArtThemeId: String = "lofi_study",
    val shortBreakArtThemeId: String = "zen_bamboo",
    val longBreakArtThemeId: String = "deep_nebula",
    val currentTaskTag: String = "Deep Work",
    val fullScreenArtMode: Boolean = false
)
