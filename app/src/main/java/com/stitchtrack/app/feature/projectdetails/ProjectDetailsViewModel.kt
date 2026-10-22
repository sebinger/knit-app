package com.stitchtrack.app.feature.projectdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stitchtrack.app.core.navigation.ProjectDetailsRoute
import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterProgress
import com.stitchtrack.app.domain.model.Note
import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.TimerSession
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.currentSessionAt
import com.stitchtrack.app.domain.model.formatDuration
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.model.progressOf
import com.stitchtrack.app.domain.model.totalAt
import com.stitchtrack.app.domain.usecase.AttachPatternUseCase
import com.stitchtrack.app.domain.usecase.CreateCounterUseCase
import com.stitchtrack.app.domain.usecase.DeleteCounterUseCase
import com.stitchtrack.app.domain.usecase.DeleteProjectUseCase
import com.stitchtrack.app.domain.usecase.RemovePatternUseCase
import com.stitchtrack.app.domain.usecase.ObserveCountersUseCase
import com.stitchtrack.app.domain.usecase.ObserveNotesUseCase
import com.stitchtrack.app.domain.usecase.ObservePatternUseCase
import com.stitchtrack.app.domain.usecase.ObserveProjectUseCase
import com.stitchtrack.app.domain.usecase.ObserveTimerSessionsUseCase
import com.stitchtrack.app.domain.usecase.PauseTimerUseCase
import com.stitchtrack.app.domain.usecase.StartTimerUseCase
import com.stitchtrack.app.domain.usecase.StopTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailsUiState(
    val project: Project? = null,
    val progress: CounterProgress = progressOf(emptyList()),
    val counters: List<Counter> = emptyList(),
    val notes: List<Note> = emptyList(),
    val pattern: PatternFile? = null,
    val currentSessionLabel: String = formatDuration(0),
    val timerLabel: String = formatDuration(0),
    val isTimerRunning: Boolean = false,
    val isTimerPaused: Boolean = false,
    val confirmDelete: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
    val showAddCounter: Boolean = false,
    val counterDraft: String = "",
    val counterError: String? = null,
    val createdCounterId: Long? = null,
    val confirmDeleteCounterId: Long? = null,
    val confirmRemovePattern: Boolean = false,
    val patternError: String? = null,
)

private data class DetailsSnapshot(
    val project: Project? = null,
    val counters: List<Counter> = emptyList(),
    val notes: List<Note> = emptyList(),
    val pattern: PatternFile? = null,
    val sessions: List<TimerSession> = emptyList(),
)

private data class DetailsFlags(
    val confirmDelete: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
    val showAddCounter: Boolean = false,
    val counterDraft: String = "",
    val counterError: String? = null,
    val createdCounterId: Long? = null,
    val confirmDeleteCounterId: Long? = null,
    val confirmRemovePattern: Boolean = false,
    val patternError: String? = null,
)

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeProject: ObserveProjectUseCase,
    observeCounters: ObserveCountersUseCase,
    observeNotes: ObserveNotesUseCase,
    observePattern: ObservePatternUseCase,
    observeTimerSessions: ObserveTimerSessionsUseCase,
    private val startTimer: StartTimerUseCase,
    private val pauseTimer: PauseTimerUseCase,
    private val stopTimer: StopTimerUseCase,
    private val deleteProject: DeleteProjectUseCase,
    private val createCounter: CreateCounterUseCase,
    private val deleteCounter: DeleteCounterUseCase,
    private val attachPattern: AttachPatternUseCase,
    private val removePattern: RemovePatternUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val projectId: Long = savedStateHandle.toRoute<ProjectDetailsRoute>().projectId
    private val flags = MutableStateFlow(DetailsFlags())

    val uiState: StateFlow<ProjectDetailsUiState> = combine(
        combine(
            observeProject(projectId),
            observeCounters(projectId),
            observeNotes(projectId),
            observePattern(projectId),
            observeTimerSessions(projectId),
        ) { project, counters, notes, pattern, sessions ->
            DetailsSnapshot(project, counters, notes, pattern, sessions)
        },
        flags,
        flow {
            while (true) {
                emit(timeProvider.nowMillis())
                delay(1_000)
            }
        },
    ) { snapshot, flagState, now ->
        val active = snapshot.sessions.lastOrNull { it.endedAt == null }
        ProjectDetailsUiState(
            project = snapshot.project,
            progress = progressOf(snapshot.counters),
            counters = snapshot.counters,
            notes = snapshot.notes,
            pattern = snapshot.pattern,
            currentSessionLabel = formatDuration(snapshot.sessions.currentSessionAt(now)),
            timerLabel = formatDuration(snapshot.sessions.totalAt(now)),
            isTimerRunning = active != null && active.pauseStartedAt == null,
            isTimerPaused = active?.pauseStartedAt != null,
            confirmDelete = flagState.confirmDelete,
            deleted = flagState.deleted,
            error = flagState.error,
            showAddCounter = flagState.showAddCounter,
            counterDraft = flagState.counterDraft,
            counterError = flagState.counterError,
            createdCounterId = flagState.createdCounterId,
            confirmDeleteCounterId = flagState.confirmDeleteCounterId,
            confirmRemovePattern = flagState.confirmRemovePattern,
            patternError = flagState.patternError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectDetailsUiState(),
    )

    fun onStart() = launchTimer { startTimer(projectId) }

    fun onPause() = launchTimer { pauseTimer(projectId) }

    fun onStop() = launchTimer { stopTimer(projectId) }

    fun onDeleteRequested() {
        flags.update { it.copy(confirmDelete = true, error = null) }
    }

    fun onDeleteDismissed() {
        flags.update { it.copy(confirmDelete = false) }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                deleteProject(projectId)
                flags.update { it.copy(confirmDelete = false, deleted = true, error = null) }
            } catch (_: Exception) {
                flags.update { it.copy(confirmDelete = false, error = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onAddCounterOpened() {
        flags.update { it.copy(showAddCounter = true, counterDraft = "", counterError = null) }
    }

    fun onAddCounterDismissed() {
        flags.update { it.copy(showAddCounter = false, counterError = null) }
    }

    fun onCounterDraftChange(value: String) {
        flags.update { it.copy(counterDraft = value, counterError = null) }
    }

    fun onCreateCounter() {
        viewModelScope.launch {
            try {
                when (val result = createCounter(projectId, flags.value.counterDraft)) {
                    is Outcome.Success -> flags.update {
                        it.copy(
                            showAddCounter = false,
                            counterDraft = "",
                            counterError = null,
                            createdCounterId = result.value,
                        )
                    }
                    is Outcome.Failure -> flags.update { it.copy(counterError = result.error.message()) }
                }
            } catch (_: Exception) {
                flags.update { it.copy(counterError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onCreatedCounterConsumed() {
        flags.update { it.copy(createdCounterId = null) }
    }

    fun onDeleteCounterRequested(counterId: Long) {
        flags.update { it.copy(confirmDeleteCounterId = counterId) }
    }

    fun onDeleteCounterDismissed() {
        flags.update { it.copy(confirmDeleteCounterId = null) }
    }

    fun onDeleteCounterConfirmed() {
        val counterId = flags.value.confirmDeleteCounterId ?: return
        viewModelScope.launch {
            try {
                deleteCounter(counterId)
                flags.update { it.copy(confirmDeleteCounterId = null) }
            } catch (_: Exception) {
                flags.update { it.copy(confirmDeleteCounterId = null, error = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onAttachPattern(fileName: String, uri: String) {
        viewModelScope.launch {
            try {
                when (val result = attachPattern(projectId, fileName, uri)) {
                    is Outcome.Success -> flags.update { it.copy(patternError = null) }
                    is Outcome.Failure -> flags.update { it.copy(patternError = result.error.message()) }
                }
            } catch (_: Exception) {
                flags.update { it.copy(patternError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onPatternErrorDismissed() {
        flags.update { it.copy(patternError = null) }
    }

    fun onRemovePatternRequested() {
        flags.update { it.copy(confirmRemovePattern = true) }
    }

    fun onRemovePatternDismissed() {
        flags.update { it.copy(confirmRemovePattern = false) }
    }

    fun onRemovePatternConfirmed() {
        viewModelScope.launch {
            try {
                removePattern(projectId)
                flags.update { it.copy(confirmRemovePattern = false) }
            } catch (_: Exception) {
                flags.update { it.copy(confirmRemovePattern = false, error = UNEXPECTED_ERROR) }
            }
        }
    }

    private fun launchTimer(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                flags.update { it.copy(error = null) }
            } catch (_: Exception) {
                flags.update { it.copy(error = UNEXPECTED_ERROR) }
            }
        }
    }
}
