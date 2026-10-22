package com.stitchtrack.app.domain.model

data class TimerSession(
    val id: Long,
    val projectId: Long,
    val startedAt: Long,
    val endedAt: Long?,
    val pausedDuration: Long,
    val pauseStartedAt: Long?,
)
