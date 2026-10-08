package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE completed = 1 ORDER BY timestamp DESC")
    fun getCompletedSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()

    // Custom Art Themes
    @Query("SELECT * FROM custom_art_themes ORDER BY createdAt DESC")
    fun getCustomArtThemes(): Flow<List<CustomArtThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomArtTheme(theme: CustomArtThemeEntity)

    @Query("DELETE FROM custom_art_themes WHERE id = :id")
    suspend fun deleteCustomArtTheme(id: String)

    // Settings
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    // Tasks Management
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("UPDATE tasks SET completedPomodoros = completedPomodoros + 1, totalMinutesSpent = totalMinutesSpent + :minutes WHERE title = :title")
    suspend fun recordPomodoroForTask(title: String, minutes: Int)

    // Subtasks / Checklist
    @Query("SELECT * FROM subtasks ORDER BY orderIndex ASC, id ASC")
    fun getAllSubtasks(): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY orderIndex ASC, id ASC")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubTaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubTaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :id")
    suspend fun deleteSubtask(id: Long)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)
}
