package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.data.MemoryCounterChangeDao
import com.stitchtrack.app.data.MemoryCounterDao
import com.stitchtrack.app.data.MemoryNoteDao
import com.stitchtrack.app.data.MemoryPatternDao
import com.stitchtrack.app.data.MemoryProjectDao
import com.stitchtrack.app.data.MemoryTimerDao
import com.stitchtrack.app.data.repository.ProjectRepositoryImpl
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.ValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateProjectUseCaseTest {
    private val projects = MemoryProjectDao()
    private val repository = ProjectRepositoryImpl(
        projectDao = projects,
        counterDao = MemoryCounterDao(),
        counterChangeDao = MemoryCounterChangeDao(),
        noteDao = MemoryNoteDao(),
        timerSessionDao = MemoryTimerDao(),
        patternFileDao = MemoryPatternDao(),
    )
    private val useCase = CreateProjectUseCase(repository, TimeProvider { 50L })

    @Test
    fun createsProjectWithTrimmedNameAndNotStartedStatus() = runTest {
        val result = useCase("  Blue scarf  ")
        val id = (result as Outcome.Success).value
        val project = repository.getProject(id)
        assertEquals("Blue scarf", project?.name)
        assertEquals(ProjectStatus.NOT_STARTED, project?.status)
        assertEquals(50L, project?.createdAt)
        assertEquals(50L, project?.updatedAt)
    }

    @Test
    fun rejectsEmptyName() = runTest {
        val result = useCase("")
        assertEquals(ValidationError.ProjectName, (result as Outcome.Failure).error)
        assertEquals(null, repository.getProject(1))
    }

    @Test
    fun rejectsBlankName() = runTest {
        val result = useCase("   ")
        assertEquals(ValidationError.ProjectName, (result as Outcome.Failure).error)
        assertEquals(null, repository.getProject(1))
    }
}
