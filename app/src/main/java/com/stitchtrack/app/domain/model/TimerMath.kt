package com.stitchtrack.app.domain.model

fun List<TimerSession>.currentSessionAt(now: Long): Long {
    val active = lastOrNull { it.endedAt == null } ?: return 0
    val endMark = active.pauseStartedAt ?: now
    return (endMark - active.startedAt - active.pausedDuration).coerceAtLeast(0)
}

fun List<TimerSession>.totalAt(now: Long): Long {
    val completed = filter { it.endedAt != null }.sumOf { session ->
        (session.endedAt!! - session.startedAt - session.pausedDuration).coerceAtLeast(0)
    }
    val active = lastOrNull { it.endedAt == null } ?: return completed
    val endMark = active.pauseStartedAt ?: now
    val activeElapsed = (endMark - active.startedAt - active.pausedDuration).coerceAtLeast(0)
    return completed + activeElapsed
}

fun formatDuration(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%d:%02d:%02d".format(hours, minutes, seconds)
}
