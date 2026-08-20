package com.example.model

enum class PomodoroMode(
    val title: String,
    val defaultMinutes: Int,
    val description: String
) {
    FOCUS("Focus Session", 25, "Deep concentration and creation"),
    SHORT_BREAK("Short Break", 5, "Breathe, stretch, and relax"),
    LONG_BREAK("Long Break", 15, "Recharge and reflect");

    fun getNextMode(completedRounds: Int, roundsBeforeLongBreak: Int = 4): PomodoroMode {
        return when (this) {
            FOCUS -> {
                if (completedRounds > 0 && completedRounds % roundsBeforeLongBreak == 0) {
                    LONG_BREAK
                } else {
                    SHORT_BREAK
                }
            }
            SHORT_BREAK -> FOCUS
            LONG_BREAK -> FOCUS
        }
    }
}
