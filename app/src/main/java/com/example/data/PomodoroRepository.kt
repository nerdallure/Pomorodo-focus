package com.example.data

import com.example.model.ArtTheme
import com.example.model.MotifType
import com.example.model.ParticleEffectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PomodoroRepository(private val dao: PomodoroDao) {

    val allSessions: Flow<List<SessionEntity>> = dao.getAllSessions()
    val completedSessions: Flow<List<SessionEntity>> = dao.getCompletedSessions()
    val userSettings: Flow<UserSettingsEntity> = dao.getUserSettings().map { it ?: UserSettingsEntity() }

    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val allSubtasks: Flow<List<SubTaskEntity>> = dao.getAllSubtasks()

    val tasksWithSubtasks: Flow<List<TaskWithSubtasks>> = combine(allTasks, allSubtasks) { tasks, subtasks ->
        val subtasksByTaskId = subtasks.groupBy { it.taskId }
        tasks.map { task ->
            TaskWithSubtasks(
                task = task,
                subtasks = subtasksByTaskId[task.id] ?: emptyList()
            )
        }
    }

    val customArtThemes: Flow<List<ArtTheme>> = dao.getCustomArtThemes().map { list ->
        list.map { entity ->
            val colors = entity.gradientColorsCsv.split(",").mapNotNull { hex ->
                hex.trim().toLongOrNull(16)
            }.ifEmpty { listOf(0xFF1B1926L, 0xFF2E2A40L) }

            val motif = try {
                MotifType.valueOf(entity.motifType)
            } catch (e: Exception) {
                MotifType.ZEN_ENSO
            }

            val particle = try {
                ParticleEffectType.valueOf(entity.particleEffect)
            } catch (e: Exception) {
                ParticleEffectType.FLOATING_SPARKS
            }

            ArtTheme(
                id = entity.id,
                name = entity.name,
                subtitle = entity.subtitle,
                category = entity.category.ifBlank { "Custom" },
                gradientColors = colors,
                ambientMoodColor = entity.ambientMoodColor,
                motifType = motif,
                particleEffect = particle,
                defaultMantra = entity.defaultMantra,
                isCustom = true
            )
        }
    }

    suspend fun logCompletedSession(
        mode: String,
        durationMinutes: Int,
        artThemeId: String,
        artThemeName: String,
        taskTag: String
    ): Long {
        val session = SessionEntity(
            mode = mode,
            durationMinutes = durationMinutes,
            artThemeId = artThemeId,
            artThemeName = artThemeName,
            taskTag = taskTag,
            timestamp = System.currentTimeMillis(),
            completed = true
        )
        return dao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) = dao.deleteSession(id)

    suspend fun clearAllHistory() = dao.clearAllSessions()

    suspend fun saveCustomArtTheme(theme: ArtTheme) {
        val colorsCsv = theme.gradientColors.joinToString(",") { it.toString(16) }
        val entity = CustomArtThemeEntity(
            id = theme.id,
            name = theme.name,
            subtitle = theme.subtitle,
            category = theme.category,
            gradientColorsCsv = colorsCsv,
            ambientMoodColor = theme.ambientMoodColor,
            motifType = theme.motifType.name,
            particleEffect = theme.particleEffect.name,
            defaultMantra = theme.defaultMantra
        )
        dao.insertCustomArtTheme(entity)
    }

    suspend fun deleteCustomArtTheme(id: String) = dao.deleteCustomArtTheme(id)

    suspend fun updateSettings(settings: UserSettingsEntity) = dao.saveUserSettings(settings)

    // Task Operations
    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)

    suspend fun deleteTask(id: Long) {
        dao.deleteSubtasksForTask(id)
        dao.deleteTask(id)
    }

    suspend fun recordPomodoroForTask(title: String, minutes: Int) {
        dao.recordPomodoroForTask(title, minutes)
    }

    // Subtask Operations
    suspend fun insertSubtask(subtask: SubTaskEntity): Long = dao.insertSubtask(subtask)

    suspend fun updateSubtask(subtask: SubTaskEntity) = dao.updateSubtask(subtask)

    suspend fun deleteSubtask(id: Long) = dao.deleteSubtask(id)
}
