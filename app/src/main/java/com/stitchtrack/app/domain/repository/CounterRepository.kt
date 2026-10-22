package com.stitchtrack.app.domain.repository

import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterChange
import kotlinx.coroutines.flow.Flow

interface CounterRepository {
    fun observeCounters(projectId: Long): Flow<List<Counter>>

    fun observeAllCounters(): Flow<List<Counter>>

    fun observeChanges(counterId: Long): Flow<List<CounterChange>>

    suspend fun getCounter(id: Long): Counter?

    suspend fun create(projectId: Long, name: String, createdAt: Long): Long

    suspend fun increment(counterId: Long, now: Long)

    suspend fun decrement(counterId: Long, now: Long)

    suspend fun undo(counterId: Long)

    suspend fun setTarget(counterId: Long, target: Int)

    suspend fun clearTarget(counterId: Long)

    suspend fun setMain(counterId: Long)

    suspend fun delete(counterId: Long)
}
