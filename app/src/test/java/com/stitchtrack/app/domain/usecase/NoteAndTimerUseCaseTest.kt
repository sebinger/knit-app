package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.data.MemoryNoteDao
import com.stitchtrack.app.data.MemoryTimerDao
import com.stitchtrack.app.data.repository.NoteRepositoryImpl
import com.stitchtrack.app.data.repository.TimerRepositoryImpl
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.model.totalAt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteUseCaseTest {
    private val repository = NoteRepositoryImpl(MemoryNoteDao())
    private val add = AddNoteUseCase(repository, TimeProvider { 20L })
    private val update = UpdateNoteUseCase(repository)

    @Test
    fun addsTrimmedNote() = runTest {
        val id = (add(1, "  keep even  ") as Outcome.Success).value
        assertEquals("keep even", repository.getNote(id)?.text)
        assertEquals(20L, repository.getNote(id)?.createdAt)
    }

    @Test
    fun rejectsBlankNote() = runTest {
        val result = add(1, "   ")
        assertEquals(ValidationError.NoteText, (result as Outcome.Failure).error)
        assertTrue(repository.observeNotes(1).first().isEmpty())
    }

    @Test
    fun editKeepsPreviousTextWhenCleared() = runTest {
        val id = (add(1, "first") as Outcome.Success).value
        val rejected = update(id, "  ")
        assertEquals(ValidationError.NoteText, (rejected as Outcome.Failure).error)
        assertEquals("first", repository.getNote(id)?.text)
        assertTrue(update(id, " second ") is Outcome.Success)
        assertEquals("second", repository.getNote(id)?.text)
    }
}

class TimerRepositoryTest {
    private val dao = MemoryTimerDao()
    private val repository = TimerRepositoryImpl(dao)

    @Test
    fun newProjectTotalIsZero() = runTest {
        assertEquals(0L, repository.observeSessions(1).first().totalAt(0))
    }

    @Test
    fun stopAddsOnlyUnpausedTime() = runTest {
        repository.start(1, now = 0)
        repository.stop(1, now = 10_000)
        assertEquals(10_000L, repository.observeSessions(1).first().totalAt(10_000))
    }

    @Test
    fun pauseExcludesPausedTimeAndResumeContinues() = runTest {
        repository.start(1, now = 0)
        repository.pause(1, now = 4_000)
        assertEquals(4_000L, repository.observeSessions(1).first().totalAt(9_000))
        repository.start(1, now = 9_000)
        repository.stop(1, now = 12_000)
        assertEquals(7_000L, repository.observeSessions(1).first().totalAt(12_000))
    }

    @Test
    fun twoSessionsAddTogether() = runTest {
        repository.start(1, now = 0)
        repository.stop(1, now = 10_000)
        repository.start(1, now = 20_000)
        repository.stop(1, now = 25_000)
        assertEquals(15_000L, repository.observeSessions(1).first().totalAt(25_000))
    }

    @Test
    fun anotherProjectIsUnaffected() = runTest {
        repository.start(1, now = 0)
        repository.stop(1, now = 5_000)
        assertEquals(0L, repository.observeSessions(2).first().totalAt(5_000))
    }
}
