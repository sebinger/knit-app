package com.stitchtrack.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.stitchtrack.app.data.local.entity.CounterChangeEntity
import com.stitchtrack.app.data.local.entity.CounterEntity
import com.stitchtrack.app.data.local.entity.NoteEntity
import com.stitchtrack.app.data.local.entity.PatternFileEntity
import com.stitchtrack.app.data.local.entity.ProjectEntity
import com.stitchtrack.app.data.local.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters ORDER BY createdAt ASC, id ASC")
    fun observeAll(): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE projectId = :projectId ORDER BY createdAt ASC, id ASC")
    fun observeByProject(projectId: Long): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE projectId = :projectId ORDER BY createdAt ASC, id ASC")
    suspend fun listByProject(projectId: Long): List<CounterEntity>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getById(id: Long): CounterEntity?

    @Insert
    suspend fun insert(counter: CounterEntity): Long

    @Update
    suspend fun update(counter: CounterEntity)

    @Query("DELETE FROM counters WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM counters WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)
}

@Dao
interface CounterChangeDao {
    @Query("SELECT * FROM counter_changes WHERE counterId = :counterId ORDER BY createdAt ASC, id ASC")
    fun observeByCounter(counterId: Long): Flow<List<CounterChangeEntity>>

    @Query(
        "SELECT * FROM counter_changes WHERE counterId = :counterId " +
            "ORDER BY createdAt DESC, id DESC LIMIT 1",
    )
    suspend fun latest(counterId: Long): CounterChangeEntity?

    @Insert
    suspend fun insert(change: CounterChangeEntity): Long

    @Query("DELETE FROM counter_changes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM counter_changes WHERE counterId = :counterId")
    suspend fun deleteByCounter(counterId: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY createdAt ASC, id ASC")
    fun observeByProject(projectId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)
}

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions WHERE projectId = :projectId ORDER BY startedAt ASC, id ASC")
    fun observeByProject(projectId: Long): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE projectId = :projectId ORDER BY startedAt ASC, id ASC")
    suspend fun listByProject(projectId: Long): List<TimerSessionEntity>

    @Query("SELECT * FROM timer_sessions WHERE projectId = :projectId AND endedAt IS NULL LIMIT 1")
    suspend fun active(projectId: Long): TimerSessionEntity?

    @Insert
    suspend fun insert(session: TimerSessionEntity): Long

    @Update
    suspend fun update(session: TimerSessionEntity)

    @Query("DELETE FROM timer_sessions WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)
}

@Dao
interface PatternFileDao {
    @Query("SELECT * FROM pattern_files WHERE projectId = :projectId LIMIT 1")
    fun observeByProject(projectId: Long): Flow<PatternFileEntity?>

    @Query("SELECT * FROM pattern_files WHERE projectId = :projectId LIMIT 1")
    suspend fun getByProject(projectId: Long): PatternFileEntity?

    @Insert
    suspend fun insert(pattern: PatternFileEntity): Long

    @Update
    suspend fun update(pattern: PatternFileEntity)

    @Query("DELETE FROM pattern_files WHERE projectId = :projectId")
    suspend fun deleteByProject(projectId: Long)
}
