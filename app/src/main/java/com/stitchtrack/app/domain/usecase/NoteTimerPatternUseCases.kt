package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.domain.model.Note
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.TimerSession
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.model.isPdfFileName
import com.stitchtrack.app.domain.repository.NoteRepository
import com.stitchtrack.app.domain.repository.PatternRepository
import com.stitchtrack.app.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(projectId: Long): Flow<List<Note>> = noteRepository.observeNotes(projectId)
}

class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, rawText: String): Outcome<Long> {
        val text = rawText.trim()
        if (text.isEmpty()) return Outcome.Failure(ValidationError.NoteText)
        return Outcome.Success(noteRepository.add(projectId, text, timeProvider.nowMillis()))
    }
}

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(noteId: Long, rawText: String): Outcome<Unit> {
        val text = rawText.trim()
        if (text.isEmpty()) return Outcome.Failure(ValidationError.NoteText)
        noteRepository.update(noteId, text)
        return Outcome.Success(Unit)
    }
}

class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(noteId: Long) {
        noteRepository.delete(noteId)
    }
}

class ObserveTimerSessionsUseCase @Inject constructor(
    private val timerRepository: TimerRepository,
) {
    operator fun invoke(projectId: Long): Flow<List<TimerSession>> = timerRepository.observeSessions(projectId)
}

class StartTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long) {
        timerRepository.start(projectId, timeProvider.nowMillis())
    }
}

class PauseTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long) {
        timerRepository.pause(projectId, timeProvider.nowMillis())
    }
}

class StopTimerUseCase @Inject constructor(
    private val timerRepository: TimerRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long) {
        timerRepository.stop(projectId, timeProvider.nowMillis())
    }
}

class ObservePatternUseCase @Inject constructor(
    private val patternRepository: PatternRepository,
) {
    operator fun invoke(projectId: Long): Flow<PatternFile?> = patternRepository.observePattern(projectId)
}

class AttachPatternUseCase @Inject constructor(
    private val patternRepository: PatternRepository,
) {
    suspend operator fun invoke(projectId: Long, fileName: String, uri: String): Outcome<Unit> {
        if (!isPdfFileName(fileName)) return Outcome.Failure(ValidationError.Pattern)
        patternRepository.replace(projectId, fileName.trim(), uri)
        return Outcome.Success(Unit)
    }
}

class RemovePatternUseCase @Inject constructor(
    private val patternRepository: PatternRepository,
) {
    suspend operator fun invoke(projectId: Long) {
        patternRepository.remove(projectId)
    }
}

class RememberPatternPageUseCase @Inject constructor(
    private val patternRepository: PatternRepository,
) {
    suspend operator fun invoke(projectId: Long, page: Int) {
        patternRepository.setLastPage(projectId, page)
    }
}
