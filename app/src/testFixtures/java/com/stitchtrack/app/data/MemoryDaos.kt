package com.stitchtrack.app.data

import com.stitchtrack.app.data.local.dao.CounterChangeDao
import com.stitchtrack.app.data.local.dao.CounterDao
import com.stitchtrack.app.data.local.dao.NoteDao
import com.stitchtrack.app.data.local.dao.PatternFileDao
import com.stitchtrack.app.data.local.dao.ProjectDao
import com.stitchtrack.app.data.local.dao.TimerSessionDao
import com.stitchtrack.app.data.local.entity.CounterChangeEntity
import com.stitchtrack.app.data.local.entity.CounterEntity
import com.stitchtrack.app.data.local.entity.NoteEntity
import com.stitchtrack.app.data.local.entity.PatternFileEntity
import com.stitchtrack.app.data.local.entity.ProjectEntity
import com.stitchtrack.app.data.local.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class MemoryProjectDao : ProjectDao {
    private val rows = MutableStateFlow<List<ProjectEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<ProjectEntity>> = rows.map { list ->
        list.sortedWith(compareByDescending<ProjectEntity> { it.createdAt }.thenByDescending { it.id })
    }

    override fun observeById(id: Long): Flow<ProjectEntity?> = rows.map { list -> list.find { it.id == id } }

    override suspend fun getById(id: Long): ProjectEntity? = rows.value.find { it.id == id }

    override suspend fun insert(project: ProjectEntity): Long {
        val id = nextId++
        rows.value = rows.value + project.copy(id = id)
        return id
    }

    override suspend fun update(project: ProjectEntity) {
        rows.value = rows.value.map { if (it.id == project.id) project else it }
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }
}

class MemoryCounterDao : CounterDao {
    private val rows = MutableStateFlow<List<CounterEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<CounterEntity>> = rows.map { list ->
        list.sortedWith(compareBy<CounterEntity> { it.createdAt }.thenBy { it.id })
    }

    override fun observeByProject(projectId: Long): Flow<List<CounterEntity>> = rows.map { list ->
        list.filter { it.projectId == projectId }
            .sortedWith(compareBy<CounterEntity> { it.createdAt }.thenBy { it.id })
    }

    override suspend fun listByProject(projectId: Long): List<CounterEntity> =
        rows.value.filter { it.projectId == projectId }
            .sortedWith(compareBy<CounterEntity> { it.createdAt }.thenBy { it.id })

    override suspend fun getById(id: Long): CounterEntity? = rows.value.find { it.id == id }

    override suspend fun insert(counter: CounterEntity): Long {
        val id = nextId++
        rows.value = rows.value + counter.copy(id = id)
        return id
    }

    override suspend fun update(counter: CounterEntity) {
        rows.value = rows.value.map { if (it.id == counter.id) counter else it }
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteByProject(projectId: Long) {
        rows.value = rows.value.filterNot { it.projectId == projectId }
    }
}

class MemoryCounterChangeDao : CounterChangeDao {
    private val rows = MutableStateFlow<List<CounterChangeEntity>>(emptyList())
    private var nextId = 1L

    override fun observeByCounter(counterId: Long): Flow<List<CounterChangeEntity>> = rows.map { list ->
        list.filter { it.counterId == counterId }
            .sortedWith(compareBy<CounterChangeEntity> { it.createdAt }.thenBy { it.id })
    }

    override suspend fun latest(counterId: Long): CounterChangeEntity? =
        rows.value.filter { it.counterId == counterId }
            .maxWithOrNull(compareBy<CounterChangeEntity> { it.createdAt }.thenBy { it.id })

    override suspend fun insert(change: CounterChangeEntity): Long {
        val id = nextId++
        rows.value = rows.value + change.copy(id = id)
        return id
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteByCounter(counterId: Long) {
        rows.value = rows.value.filterNot { it.counterId == counterId }
    }
}

class MemoryNoteDao : NoteDao {
    private val rows = MutableStateFlow<List<NoteEntity>>(emptyList())
    private var nextId = 1L

    override fun observeByProject(projectId: Long): Flow<List<NoteEntity>> = rows.map { list ->
        list.filter { it.projectId == projectId }
            .sortedWith(compareBy<NoteEntity> { it.createdAt }.thenBy { it.id })
    }

    override suspend fun getById(id: Long): NoteEntity? = rows.value.find { it.id == id }

    override suspend fun insert(note: NoteEntity): Long {
        val id = nextId++
        rows.value = rows.value + note.copy(id = id)
        return id
    }

    override suspend fun update(note: NoteEntity) {
        rows.value = rows.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun deleteById(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteByProject(projectId: Long) {
        rows.value = rows.value.filterNot { it.projectId == projectId }
    }
}

class MemoryTimerDao : TimerSessionDao {
    private val rows = MutableStateFlow<List<TimerSessionEntity>>(emptyList())
    private var nextId = 1L

    override fun observeByProject(projectId: Long): Flow<List<TimerSessionEntity>> = rows.map { list ->
        list.filter { it.projectId == projectId }
            .sortedWith(compareBy<TimerSessionEntity> { it.startedAt }.thenBy { it.id })
    }

    override suspend fun listByProject(projectId: Long): List<TimerSessionEntity> =
        rows.value.filter { it.projectId == projectId }

    override suspend fun active(projectId: Long): TimerSessionEntity? =
        rows.value.find { it.projectId == projectId && it.endedAt == null }

    override suspend fun insert(session: TimerSessionEntity): Long {
        val id = nextId++
        rows.value = rows.value + session.copy(id = id)
        return id
    }

    override suspend fun update(session: TimerSessionEntity) {
        rows.value = rows.value.map { if (it.id == session.id) session else it }
    }

    override suspend fun deleteByProject(projectId: Long) {
        rows.value = rows.value.filterNot { it.projectId == projectId }
    }
}

class MemoryPatternDao : PatternFileDao {
    private val rows = MutableStateFlow<List<PatternFileEntity>>(emptyList())
    private var nextId = 1L

    override fun observeByProject(projectId: Long): Flow<PatternFileEntity?> =
        rows.map { list -> list.find { it.projectId == projectId } }

    override suspend fun getByProject(projectId: Long): PatternFileEntity? =
        rows.value.find { it.projectId == projectId }

    override suspend fun insert(pattern: PatternFileEntity): Long {
        val id = nextId++
        rows.value = rows.value + pattern.copy(id = id)
        return id
    }

    override suspend fun update(pattern: PatternFileEntity) {
        rows.value = rows.value.map { if (it.id == pattern.id) pattern else it }
    }

    override suspend fun deleteByProject(projectId: Long) {
        rows.value = rows.value.filterNot { it.projectId == projectId }
    }
}
