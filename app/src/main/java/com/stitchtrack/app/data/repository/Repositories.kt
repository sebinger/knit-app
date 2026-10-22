package com.stitchtrack.app.data.repository

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
import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterChange
import com.stitchtrack.app.domain.model.Note
import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.TimerSession
import com.stitchtrack.app.domain.repository.CounterRepository
import com.stitchtrack.app.domain.repository.NoteRepository
import com.stitchtrack.app.domain.repository.PatternRepository
import com.stitchtrack.app.domain.repository.ProjectRepository
import com.stitchtrack.app.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val counterDao: CounterDao,
    private val counterChangeDao: CounterChangeDao,
    private val noteDao: NoteDao,
    private val timerSessionDao: TimerSessionDao,
    private val patternFileDao: PatternFileDao,
) : ProjectRepository {
    override fun observeProjects(): Flow<List<Project>> =
        projectDao.observeAll().map { projects -> projects.map { it.toDomain() } }

    override fun observeProject(id: Long): Flow<Project?> =
        projectDao.observeById(id).map { project -> project?.toDomain() }

    override suspend fun getProject(id: Long): Project? = projectDao.getById(id)?.toDomain()

    override suspend fun insert(
        name: String,
        status: ProjectStatus,
        createdAt: Long,
        updatedAt: Long,
    ): Long = projectDao.insert(
        ProjectEntity(
            name = name,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),
    )

    override suspend fun update(project: Project) {
        projectDao.update(
            ProjectEntity(
                id = project.id,
                name = project.name,
                status = project.status,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                itemType = project.itemType,
                coverUri = project.coverUri,
            ),
        )
    }

    override suspend fun delete(id: Long) {
        counterDao.listByProject(id).forEach { counter ->
            counterChangeDao.deleteByCounter(counter.id)
        }
        counterDao.deleteByProject(id)
        noteDao.deleteByProject(id)
        timerSessionDao.deleteByProject(id)
        patternFileDao.deleteByProject(id)
        projectDao.deleteById(id)
    }
}

class CounterRepositoryImpl @Inject constructor(
    private val counterDao: CounterDao,
    private val counterChangeDao: CounterChangeDao,
) : CounterRepository {
    override fun observeCounters(projectId: Long): Flow<List<Counter>> =
        counterDao.observeByProject(projectId).map { counters -> counters.map { it.toDomain() } }

    override fun observeAllCounters(): Flow<List<Counter>> =
        counterDao.observeAll().map { counters -> counters.map { it.toDomain() } }

    override fun observeChanges(counterId: Long): Flow<List<CounterChange>> =
        counterChangeDao.observeByCounter(counterId).map { changes -> changes.map { it.toDomain() } }

    override suspend fun getCounter(id: Long): Counter? = counterDao.getById(id)?.toDomain()

    override suspend fun create(projectId: Long, name: String, createdAt: Long): Long {
        val isMain = counterDao.listByProject(projectId).none { it.isMain }
        return counterDao.insert(
            CounterEntity(
                projectId = projectId,
                name = name,
                value = 0,
                target = null,
                isMain = isMain,
                createdAt = createdAt,
            ),
        )
    }

    override suspend fun increment(counterId: Long, now: Long) {
        val counter = counterDao.getById(counterId) ?: return
        counterDao.update(counter.copy(value = counter.value + 1))
        counterChangeDao.insert(
            CounterChangeEntity(counterId = counterId, delta = 1, createdAt = now),
        )
    }

    override suspend fun decrement(counterId: Long, now: Long) {
        val counter = counterDao.getById(counterId) ?: return
        if (counter.value <= 0) return
        counterDao.update(counter.copy(value = counter.value - 1))
        counterChangeDao.insert(
            CounterChangeEntity(counterId = counterId, delta = -1, createdAt = now),
        )
    }

    override suspend fun undo(counterId: Long) {
        val change = counterChangeDao.latest(counterId) ?: return
        val counter = counterDao.getById(counterId) ?: return
        counterDao.update(counter.copy(value = (counter.value - change.delta).coerceAtLeast(0)))
        counterChangeDao.deleteById(change.id)
    }

    override suspend fun setTarget(counterId: Long, target: Int) {
        val counter = counterDao.getById(counterId) ?: return
        counterDao.update(counter.copy(target = target))
    }

    override suspend fun clearTarget(counterId: Long) {
        val counter = counterDao.getById(counterId) ?: return
        counterDao.update(counter.copy(target = null))
    }

    override suspend fun setMain(counterId: Long) {
        val selected = counterDao.getById(counterId) ?: return
        counterDao.listByProject(selected.projectId).forEach { counter ->
            counterDao.update(counter.copy(isMain = counter.id == counterId))
        }
    }

    override suspend fun delete(counterId: Long) {
        val counter = counterDao.getById(counterId) ?: return
        counterChangeDao.deleteByCounter(counterId)
        counterDao.deleteById(counterId)
        if (counter.isMain) {
            val next = counterDao.listByProject(counter.projectId).minWithOrNull(
                compareBy<CounterEntity> { it.createdAt }.thenBy { it.id },
            )
            if (next != null) {
                counterDao.update(next.copy(isMain = true))
            }
        }
    }
}

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
) : NoteRepository {
    override fun observeNotes(projectId: Long): Flow<List<Note>> =
        noteDao.observeByProject(projectId).map { notes -> notes.map { it.toDomain() } }

    override suspend fun getNote(id: Long): Note? = noteDao.getById(id)?.toDomain()

    override suspend fun add(projectId: Long, text: String, createdAt: Long): Long =
        noteDao.insert(NoteEntity(projectId = projectId, text = text, createdAt = createdAt))

    override suspend fun update(id: Long, text: String) {
        val note = noteDao.getById(id) ?: return
        noteDao.update(note.copy(text = text))
    }

    override suspend fun delete(id: Long) {
        noteDao.deleteById(id)
    }
}

class TimerRepositoryImpl @Inject constructor(
    private val timerSessionDao: TimerSessionDao,
) : TimerRepository {
    override fun observeSessions(projectId: Long): Flow<List<TimerSession>> =
        timerSessionDao.observeByProject(projectId).map { sessions -> sessions.map { it.toDomain() } }

    override suspend fun start(projectId: Long, now: Long) {
        val active = timerSessionDao.active(projectId)
        if (active == null) {
            timerSessionDao.insert(
                TimerSessionEntity(
                    projectId = projectId,
                    startedAt = now,
                    endedAt = null,
                    pausedDuration = 0,
                    pauseStartedAt = null,
                ),
            )
            return
        }
        val pauseStartedAt = active.pauseStartedAt ?: return
        val extraPause = (now - pauseStartedAt).coerceAtLeast(0)
        timerSessionDao.update(
            active.copy(
                pausedDuration = active.pausedDuration + extraPause,
                pauseStartedAt = null,
            ),
        )
    }

    override suspend fun pause(projectId: Long, now: Long) {
        val active = timerSessionDao.active(projectId) ?: return
        if (active.pauseStartedAt != null) return
        timerSessionDao.update(active.copy(pauseStartedAt = now))
    }

    override suspend fun stop(projectId: Long, now: Long) {
        val active = timerSessionDao.active(projectId) ?: return
        val extraPause = active.pauseStartedAt?.let { (now - it).coerceAtLeast(0) } ?: 0
        timerSessionDao.update(
            active.copy(
                endedAt = now,
                pausedDuration = active.pausedDuration + extraPause,
                pauseStartedAt = null,
            ),
        )
    }
}

class PatternRepositoryImpl @Inject constructor(
    private val patternFileDao: PatternFileDao,
) : PatternRepository {
    override fun observePattern(projectId: Long): Flow<PatternFile?> =
        patternFileDao.observeByProject(projectId).map { pattern -> pattern?.toDomain() }

    override suspend fun replace(projectId: Long, fileName: String, uri: String) {
        patternFileDao.deleteByProject(projectId)
        patternFileDao.insert(
            PatternFileEntity(
                projectId = projectId,
                fileName = fileName,
                uri = uri,
                lastPage = 1,
            ),
        )
    }

    override suspend fun remove(projectId: Long) {
        patternFileDao.deleteByProject(projectId)
    }

    override suspend fun setLastPage(projectId: Long, page: Int) {
        val pattern = patternFileDao.getByProject(projectId) ?: return
        patternFileDao.update(pattern.copy(lastPage = page.coerceAtLeast(1)))
    }
}
