package com.stitchtrack.app.domain.repository

import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.TimerSession
import kotlinx.coroutines.flow.Flow

interface TimerRepository {
    fun observeSessions(projectId: Long): Flow<List<TimerSession>>

    suspend fun start(projectId: Long, now: Long)

    suspend fun pause(projectId: Long, now: Long)

    suspend fun stop(projectId: Long, now: Long)
}

interface PatternRepository {
    fun observePattern(projectId: Long): Flow<PatternFile?>

    suspend fun replace(projectId: Long, fileName: String, uri: String)

    suspend fun remove(projectId: Long)

    suspend fun setLastPage(projectId: Long, page: Int)
}
