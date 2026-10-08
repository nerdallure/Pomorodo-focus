package com.example.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubTaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MintCyan
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TaskViewMode(val title: String) {
    TODAY("Today"),
    SCHEDULE("Schedule"),
    HABITS("Habits"),
    PROJECTS("Projects"),
    DONE("Completed")
}

val DefaultProjects = listOf("Work", "Study", "Homework", "Housework", "Personal", "Health")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasksWithSubtasks: List<TaskWithSubtasks>,
    onToggleTask: (TaskWithSubtasks) -> Unit,
    onToggleSubtask: (SubTaskEntity) -> Unit,
    onAddSubtask: (taskId: Long, title: String) -> Unit,
    onDeleteSubtask: (subtaskId: Long) -> Unit,
    onDeleteTask: (taskId: Long) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onCreateTask: (
        title: String,
        description: String,
        project: String,
        priority: String,
        dueDateMillis: Long?,
        isRecurring: Boolean,
        recurrenceRule: String,
        reminderMillis: Long?,
        reminderDisplay: String?,
        estimatedPomodoros: Int,
        subtaskTitles: List<String>,
        isHabit: Boolean
    ) -> Unit,
    onFocusTask: (TaskEntity) -> Unit
) {
    var selectedView by remember { mutableStateOf(TaskViewMode.TODAY) }
    var selectedProjectFilter by remember { mutableStateOf<String?>("All") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskWithSubtasks?>(null) }

    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayStartMillis = todayCalendar.timeInMillis
    val todayEndMillis = todayStartMillis + 86400000L

    // Filter tasks based on view mode and project
    val filteredTasks = tasksWithSubtasks.filter { item ->
        val matchesProject = selectedProjectFilter == "All" || item.task.project == selectedProjectFilter

        val matchesView = when (selectedView) {
            TaskViewMode.TODAY -> {
                !item.task.isCompleted && (
                    item.task.isHabit ||
                    item.task.dueDateMillis == null ||
                    item.task.dueDateMillis <= todayEndMillis
                )
            }
            TaskViewMode.SCHEDULE -> {
                !item.task.isCompleted && item.task.dueDateMillis != null
            }
            TaskViewMode.HABITS -> {
                item.task.isHabit || item.task.isRecurring
            }
            TaskViewMode.PROJECTS -> {
                !item.task.isCompleted
            }
            TaskViewMode.DONE -> {
                item.task.isCompleted
            }
        }

        matchesProject && matchesView
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // View Mode Navigation Tabs
            SecondaryTabRow(
                selectedTabIndex = selectedView.ordinal,
                containerColor = DarkSurface,
                contentColor = CoralPrimary,
                modifier = Modifier.fillMaxWidth().testTag("tasks_view_tabs")
            ) {
                TaskViewMode.values().forEach { mode ->
                    Tab(
                        selected = selectedView == mode,
                        onClick = { selectedView = mode },
                        text = {
                            Text(
                                text = mode.title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedView == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedView == mode) CoralPrimary else TextSecondary
                            )
                        }
                    )
                }
            }

            // Project / Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filterOptions = listOf("All") + DefaultProjects
                filterOptions.forEach { proj ->
                    val isSelected = selectedProjectFilter == proj
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedProjectFilter = proj },
                        label = {
                            Text(
                                text = proj,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPrimary,
                            containerColor = DarkSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CoralPrimary else Color.Transparent
                        ),
                        modifier = Modifier.height(30.dp).testTag("filter_chip_$proj")
                    )
                }
            }

            // Summary Header Badge
            val pendingCount = tasksWithSubtasks.count { !it.task.isCompleted }
            val completedCount = tasksWithSubtasks.count { it.task.isCompleted }
            val totalPlannedPomodoros = tasksWithSubtasks.filter { !it.task.isCompleted }.sumOf { it.task.estimatedPomodoros }
            val completedPomodoros = tasksWithSubtasks.sumOf { it.task.completedPomodoros }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedView) {
                                TaskViewMode.TODAY -> "Focus To-Do • Today"
                                TaskViewMode.SCHEDULE -> "Schedule Planner"
                                TaskViewMode.HABITS -> "Habit Tracker"
                                TaskViewMode.PROJECTS -> "Projects Organizer"
                                TaskViewMode.DONE -> "Accomplishments"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "$pendingCount pending • $completedCount completed",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CoralPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Pomodoros",
                                tint = CoralPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🍅 $completedPomodoros / $totalPlannedPomodoros done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CoralPrimary
                            )
                        }
                    }
                }
            }

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedView == TaskViewMode.DONE) "No completed tasks yet" else "No tasks in this view",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button below to create your next To-Do item.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("tasks_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.task.id }) { item ->
                        TaskItemCard(
                            taskWithSubtasks = item,
                            onToggleTask = { onToggleTask(item) },
                            onToggleSubtask = onToggleSubtask,
                            onAddSubtask = { subTitle -> onAddSubtask(item.task.id, subTitle) },
                            onDeleteSubtask = onDeleteSubtask,
                            onDeleteTask = { onDeleteTask(item.task.id) },
                            onEditTask = { taskToEdit = item },
                            onFocusTask = { onFocusTask(item.task) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add New Task
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = CoralPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_task_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(24.dp))
        }
    }

    // Create Task Dialog
    if (showCreateDialog) {
        TaskEditorDialog(
            initialTask = null,
            initialSubtasks = emptyList(),
            onDismiss = { showCreateDialog = false },
            onSave = { title, desc, project, priority, dueDate, isRec, recRule, reminderTime, reminderStr, pomodoros, subtasks, isHabit ->
                onCreateTask(
                    title, desc, project, priority, dueDate, isRec, recRule, reminderTime, reminderStr, pomodoros, subtasks, isHabit
                )
                showCreateDialog = false
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { item ->
        TaskEditorDialog(
            initialTask = item.task,
            initialSubtasks = item.subtasks.map { it.title },
            onDismiss = { taskToEdit = null },
            onSave = { title, desc, project, priority, dueDate, isRec, recRule, reminderTime, reminderStr, pomodoros, subtasks, isHabit ->
                onUpdateTask(
                    item.task.copy(
                        title = title,
                        description = desc,
                        project = project,
                        priority = priority,
                        dueDateMillis = dueDate,
                        isRecurring = isRec,
                        recurrenceRule = recRule,
                        reminderMillis = reminderTime,
                        reminderDisplay = reminderStr,
                        estimatedPomodoros = pomodoros,
                        isHabit = isHabit
                    )
                )
                taskToEdit = null
            }
        )
    }
}

/**
 * Individual Card representing a Task with checklist, priority, recurrence, time tracker and reminders.
 */
@Composable
fun TaskItemCard(
    taskWithSubtasks: TaskWithSubtasks,
    onToggleTask: () -> Unit,
    onToggleSubtask: (SubTaskEntity) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (Long) -> Unit,
    onDeleteTask: () -> Unit,
    onEditTask: () -> Unit,
    onFocusTask: () -> Unit
) {
    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    var expandedChecklist by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var showAddSubtaskField by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority.uppercase()) {
        "HIGH" -> CoralPrimary
        "MEDIUM" -> AmberGold
        "LOW" -> SkyBlue
        else -> OutlineBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) DarkSurface.copy(alpha = 0.6f) else DarkSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority Strip Indicator
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(priorityColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Checkbox
                IconButton(
                    onClick = onToggleTask,
                    modifier = Modifier.size(32.dp).testTag("task_checkbox_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle completion",
                        tint = if (task.isCompleted) MintCyan else priorityColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Title and badges
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextMuted else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Metadata Badges Row (Project, Priority, Recurrence, Reminder, Pomodoros)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Project Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceElevated
                        ) {
                            Text(
                                text = task.project,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Priority Badge
                        if (task.priority.uppercase() != "NONE") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = priorityColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = when (task.priority.uppercase()) {
                                        "HIGH" -> "🔴 High"
                                        "MEDIUM" -> "🟠 Medium"
                                        "LOW" -> "🔵 Low"
                                        else -> ""
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = priorityColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Recurring / Habit Badge
                        if (task.isHabit || task.isRecurring) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberGold.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (task.isHabit) Icons.Default.LocalFireDepartment else Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (task.isHabit) "🔥 ${task.habitStreak} Streak" else formatRecurrenceRule(task.recurrenceRule),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AmberGold
                                    )
                                }
                            }
                        }

                        // Reminder Badge
                        task.reminderDisplay?.let { rem ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SkyBlue.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = SkyBlue,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = rem,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SkyBlue
                                    )
                                }
                            }
                        }

                        // Time Tracker / Pomodoro Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🍅 ${task.completedPomodoros}/${task.estimatedPomodoros}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Focus Action Button (Launches timer with this task objective)
                if (!task.isCompleted) {
                    IconButton(
                        onClick = onFocusTask,
                        modifier = Modifier
                            .size(34.dp)
                            .background(CoralPrimary.copy(alpha = 0.2f), CircleShape)
                            .testTag("focus_task_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Focus on this task",
                            tint = CoralPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Subtask Progress Bar (if task has subtasks)
            if (subtasks.isNotEmpty()) {
                val completedSubtasks = subtasks.count { it.isCompleted }
                val progressFraction = completedSubtasks.toFloat() / subtasks.size.toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedChecklist = !expandedChecklist }
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Checklist: $completedSubtasks of ${subtasks.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .width(70.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MintCyan,
                            trackColor = DarkSurfaceElevated
                        )
                    }

                    Icon(
                        imageVector = if (expandedChecklist) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle subtasks",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Checklist Section
            AnimatedVisibility(
                visible = expandedChecklist,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { onToggleSubtask(subtask) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MintCyan,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = DarkBackground
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtask.title,
                                fontSize = 12.sp,
                                color = if (subtask.isCompleted) TextMuted else TextPrimary,
                                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSubtask(subtask.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete subtask",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Add Subtask field
                    if (showAddSubtaskField) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("New checklist item...", fontSize = 11.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(44.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CoralPrimary,
                                    unfocusedBorderColor = OutlineBorder
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (newSubtaskText.isNotBlank()) {
                                        onAddSubtask(newSubtaskText)
                                        newSubtaskText = ""
                                        showAddSubtaskField = false
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        onAddSubtask(newSubtaskText)
                                        newSubtaskText = ""
                                        showAddSubtaskField = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save subtask",
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { showAddSubtaskField = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Checklist Item", fontSize = 11.sp, color = CoralPrimary)
                        }
                    }
                }
            }

            // Quick Footer Actions (Edit, Delete, Add Subtask toggle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (subtasks.isEmpty()) {
                    TextButton(
                        onClick = {
                            expandedChecklist = true
                            showAddSubtaskField = true
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("+ Checklist", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                IconButton(onClick = onEditTask, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(onClick = onDeleteTask, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Task Creation & Editing Dialog with priority, recurrence, reminder, subtasks, and pomodoro estimate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    initialTask: TaskEntity?,
    initialSubtasks: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        project: String,
        priority: String,
        dueDateMillis: Long?,
        isRecurring: Boolean,
        recurrenceRule: String,
        reminderMillis: Long?,
        reminderDisplay: String?,
        estimatedPomodoros: Int,
        subtasks: List<String>,
        isHabit: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var selectedProject by remember { mutableStateOf(initialTask?.project ?: "Work") }
    var selectedPriority by remember { mutableStateOf(initialTask?.priority ?: "MEDIUM") }
    var isRecurring by remember { mutableStateOf(initialTask?.isRecurring ?: false) }
    var recurrenceRule by remember { mutableStateOf(initialTask?.recurrenceRule ?: "NONE") }
    var isHabit by remember { mutableStateOf(initialTask?.isHabit ?: false) }
    var reminderDisplay by remember { mutableStateOf(initialTask?.reminderDisplay) }
    var estimatedPomodoros by remember { mutableStateOf(initialTask?.estimatedPomodoros ?: 2) }

    // Subtasks builder state
    var subtaskList by remember { mutableStateOf(initialSubtasks) }
    var tempSubtaskInput by remember { mutableStateOf("") }

    val priorities = listOf("HIGH" to "🔴 High", "MEDIUM" to "🟠 Med", "LOW" to "🔵 Low", "NONE" to "⚪ None")
    val recurrenceOptions = listOf(
        "NONE" to "No Repeat",
        "DAILY" to "Daily",
        "WEEKDAYS" to "Weekdays (M-F)",
        "EVERY_MONDAY" to "Every Monday",
        "WEEKLY" to "Weekly",
        "MONTHLY" to "Monthly"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTask == null) "Create New To-Do" else "Edit Task",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = OutlineBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_task_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Objective (Optional)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = OutlineBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_task_description")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Project / Category Selector
                Text("Project / Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultProjects.forEach { proj ->
                        val isSelected = selectedProject == proj
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedProject = proj },
                            label = { Text(proj, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                containerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Level Selector
                Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { (code, label) ->
                        val isSelected = selectedPriority == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriority = code },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (code) {
                                    "HIGH" -> CoralPrimary
                                    "MEDIUM" -> AmberGold
                                    "LOW" -> SkyBlue
                                    else -> OutlineBorder
                                },
                                containerColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring & Habit Tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Recurring Task / Habit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Auto-schedules next occurrence & tracks streak", fontSize = 10.sp, color = TextSecondary)
                    }
                    Checkbox(
                        checked = isRecurring || isHabit,
                        onCheckedChange = { checked ->
                            isRecurring = checked
                            isHabit = checked
                            if (!checked) recurrenceRule = "NONE"
                            else if (recurrenceRule == "NONE") recurrenceRule = "DAILY"
                        },
                        colors = CheckboxDefaults.colors(checkedColor = AmberGold)
                    )
                }

                if (isRecurring || isHabit) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recurrenceOptions.forEach { (rule, label) ->
                            val isSelected = recurrenceRule == rule
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    recurrenceRule = rule
                                    isRecurring = rule != "NONE"
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGold,
                                    containerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reminders
                Text("Reminder", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                val reminderPresets = listOf("None", "09:00 AM", "02:00 PM", "07:00 PM")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reminderPresets.forEach { timeStr ->
                        val isSelected = (timeStr == "None" && reminderDisplay == null) || reminderDisplay == timeStr
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                reminderDisplay = if (timeStr == "None") null else timeStr
                            },
                            label = { Text(timeStr, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SkyBlue,
                                containerColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estimated Pomodoro Focus Units
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Focus Time Goal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Text("🍅 $estimatedPomodoros Pomodoros (${estimatedPomodoros * 25}m)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (estimatedPomodoros > 1) estimatedPomodoros-- },
                        modifier = Modifier.size(36.dp).background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("$estimatedPomodoros", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { if (estimatedPomodoros < 12) estimatedPomodoros++ },
                        modifier = Modifier.size(36.dp).background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checklist / Subtasks builder
                Text("Checklist / Sub-tasks", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))

                subtaskList.forEachIndexed { idx, sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MintCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(sub, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                subtaskList = subtaskList.toMutableList().also { it.removeAt(idx) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempSubtaskInput,
                        onValueChange = { tempSubtaskInput = it },
                        placeholder = { Text("Add subtask...", fontSize = 11.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = OutlineBorder
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (tempSubtaskInput.isNotBlank()) {
                                subtaskList = subtaskList + tempSubtaskInput.trim()
                                tempSubtaskInput = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (tempSubtaskInput.isNotBlank()) {
                                subtaskList = subtaskList + tempSubtaskInput.trim()
                                tempSubtaskInput = ""
                            }
                        },
                        modifier = Modifier.size(36.dp).background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add subtask", tint = CoralPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            description.trim(),
                            selectedProject,
                            selectedPriority,
                            System.currentTimeMillis(),
                            isRecurring,
                            recurrenceRule,
                            if (reminderDisplay != null) System.currentTimeMillis() else null,
                            reminderDisplay,
                            estimatedPomodoros,
                            subtaskList,
                            isHabit
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("Save To-Do", fontWeight = FontWeight.Bold)
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

fun formatRecurrenceRule(rule: String): String {
    return when (rule.uppercase()) {
        "DAILY" -> "🔁 Daily"
        "WEEKDAYS" -> "🔁 Weekdays"
        "EVERY_MONDAY" -> "🔁 Every Mon"
        "WEEKLY" -> "🔁 Weekly"
        "MONTHLY" -> "🔁 Monthly"
        else -> "🔁 Recurring"
    }
}
