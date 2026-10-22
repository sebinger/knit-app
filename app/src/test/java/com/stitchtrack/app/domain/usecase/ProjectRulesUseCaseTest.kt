package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.data.MemoryCounterChangeDao
import com.stitchtrack.app.data.MemoryCounterDao
import com.stitchtrack.app.data.MemoryNoteDao
import com.stitchtrack.app.data.MemoryPatternDao
import com.stitchtrack.app.data.MemoryProjectDao
import com.stitchtrack.app.data.MemoryTimerDao
import com.stitchtrack.app.data.repository.CounterRepositoryImpl
import com.stitchtrack.app.data.repository.NoteRepositoryImpl
import com.stitchtrack.app.data.repository.PatternRepositoryImpl
import com.stitchtrack.app.data.repository.ProjectRepositoryImpl
import com.stitchtrack.app.data.repository.TimerRepositoryImpl
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.model.progressOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectRulesUseCaseTest {
    private val projectDao = MemoryProjectDao()
    private val counterDao = MemoryCounterDao()
    private val changeDao = MemoryCounterChangeDao()
    private val noteDao = MemoryNoteDao()
    private val timerDao = MemoryTimerDao()
    private val patternDao = MemoryPatternDao()
    private val projects = ProjectRepositoryImpl(
        projectDao,
        counterDao,
        changeDao,
        noteDao,
        timerDao,
        patternDao,
    )
    private val counters = CounterRepositoryImpl(counterDao, changeDao)
    private val notes = NoteRepositoryImpl(noteDao)
    private val timers = TimerRepositoryImpl(timerDao)
    private val patterns = PatternRepositoryImpl(patternDao)
    private val time = TimeProvider { 5L }

    @Test
    fun newestProjectIsFirst() = runTest {
        projects.insert("Older", ProjectStatus.NOT_STARTED, createdAt = 1, updatedAt = 1)
        projects.insert("Newer", ProjectStatus.NOT_STARTED, createdAt = 2, updatedAt = 2)
        val names = projects.observeProjects().first().map { it.name }
        assertEquals(listOf("Newer", "Older"), names)
    }

    @Test
    fun renameRejectsBlankAndKeepsPreviousName() = runTest {
        val id = projects.insert("Hat", ProjectStatus.NOT_STARTED, 1, 1)
        val result = RenameProjectUseCase(projects, time)(id, "   ")
        assertEquals(ValidationError.ProjectName, (result as Outcome.Failure).error)
        assertEquals("Hat", projects.getProject(id)?.name)
    }

    @Test
    fun statusChangeDoesNotChangeProgress() = runTest {
        val projectId = projects.insert("Hat", ProjectStatus.NOT_STARTED, 1, 1)
        val counterId = counters.create(projectId, "Rows", 1)
        counters.setTarget(counterId, 100)
        repeat(25) { counters.increment(counterId, 2) }
        UpdateProjectStatusUseCase(projects, time)(projectId, ProjectStatus.COMPLETED)
        val progress = progressOf(counters.observeCounters(projectId).first())
        assertEquals(25, progress.percentage)
        assertEquals(ProjectStatus.COMPLETED, projects.getProject(projectId)?.status)
    }

    @Test
    fun deleteProjectRemovesRelatedData() = runTest {
        val projectId = projects.insert("Hat", ProjectStatus.NOT_STARTED, 1, 1)
        val counterId = counters.create(projectId, "Rows", 1)
        counters.increment(counterId, 2)
        notes.add(projectId, "note", 3)
        timers.start(projectId, 4)
        patterns.replace(projectId, "chart.pdf", "content://chart")
        projects.delete(projectId)
        assertNull(projects.getProject(projectId))
        assertTrue(counters.observeCounters(projectId).first().isEmpty())
        assertTrue(notes.observeNotes(projectId).first().isEmpty())
        assertTrue(timers.observeSessions(projectId).first().isEmpty())
        assertNull(patterns.observePattern(projectId).first())
        assertTrue(counters.observeChanges(counterId).first().isEmpty())
    }

    @Test
    fun patternMustBePdfAndReplaceResetsPage() = runTest {
        val projectId = projects.insert("Hat", ProjectStatus.NOT_STARTED, 1, 1)
        val attach = AttachPatternUseCase(patterns)
        assertEquals(ValidationError.Pattern, (attach(projectId, "photo.png", "content://photo") as Outcome.Failure).error)
        assertTrue(attach(projectId, "chart.PDF", "content://chart") is Outcome.Success)
        patterns.setLastPage(projectId, 4)
        assertEquals(4, patterns.observePattern(projectId).first()?.lastPage)
        attach(projectId, "next.pdf", "content://next")
        assertEquals(1, patterns.observePattern(projectId).first()?.lastPage)
        assertEquals("next.pdf", patterns.observePattern(projectId).first()?.fileName)
    }
}
