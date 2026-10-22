package com.stitchtrack.app.feature.counters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stitchtrack.app.core.navigation.CountersRoute
import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterChange
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.usecase.ClearCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.CreateCounterUseCase
import com.stitchtrack.app.domain.usecase.DecrementCounterUseCase
import com.stitchtrack.app.domain.usecase.DeleteCounterUseCase
import com.stitchtrack.app.domain.usecase.IncrementCounterUseCase
import com.stitchtrack.app.domain.usecase.ObserveCounterChangesUseCase
import com.stitchtrack.app.domain.usecase.ObserveCountersUseCase
import com.stitchtrack.app.domain.usecase.SetCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.SetMainCounterUseCase
import com.stitchtrack.app.domain.usecase.UndoCounterChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CounterUiState(
    val counters: List<Counter> = emptyList(),
    val selectedId: Long? = null,
    val draftName: String = "",
    val nameError: String? = null,
    val value: Int = 0,
    val targetInput: String = "",
    val targetError: String? = null,
    val canUndo: Boolean = false,
    val undoLabel: String = "Undo",
    val isMain: Boolean = false,
    val confirmDelete: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CounterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCounters: ObserveCountersUseCase,
    observeCounterChanges: ObserveCounterChangesUseCase,
    private val createCounter: CreateCounterUseCase,
    private val incrementCounter: IncrementCounterUseCase,
    private val decrementCounter: DecrementCounterUseCase,
    private val undoCounterChange: UndoCounterChangeUseCase,
    private val setCounterTarget: SetCounterTargetUseCase,
    private val clearCounterTarget: ClearCounterTargetUseCase,
    private val setMainCounter: SetMainCounterUseCase,
    private val deleteCounter: DeleteCounterUseCase,
) : ViewModel() {
    private val projectId: Long = savedStateHandle.toRoute<CountersRoute>().projectId
    private val routeCounterId: Long = savedStateHandle.get<Long>("counterId") ?: 0L
    private val editor = MutableStateFlow(
        CounterUiState(selectedId = routeCounterId.takeIf { it != 0L }),
    )

    private val latestChange = editor
        .map { it.selectedId }
        .distinctUntilChanged()
        .flatMapLatest { counterId ->
            if (counterId == null) {
                flowOf<CounterChange?>(null)
            } else {
                observeCounterChanges(counterId).map { changes -> changes.lastOrNull() }
            }
        }

    val uiState: StateFlow<CounterUiState> = combine(editor, latestChange) { current, change ->
        val name = current.counters.firstOrNull { it.id == current.selectedId }?.name.orEmpty()
        val undoLabel = if (change == null || name.isEmpty()) {
            "Undo"
        } else {
            val signed = if (change.delta > 0) "+${change.delta}" else change.delta.toString()
            "Undo $signed $name"
        }
        current.copy(canUndo = change != null, undoLabel = undoLabel)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CounterUiState(),
    )

    fun onDraftNameChange(name: String) {
        editor.update { it.copy(draftName = name, nameError = null) }
    }

    fun onSelect(counterId: Long) {
        val counter = editor.value.counters.firstOrNull { it.id == counterId } ?: return
        editor.update {
            it.copy(
                selectedId = counterId,
                value = counter.value,
                isMain = counter.isMain,
                targetInput = counter.target?.toString().orEmpty(),
                targetError = null,
            )
        }
    }

    fun onAdd() {
        viewModelScope.launch {
            try {
                when (val result = createCounter(projectId, editor.value.draftName)) {
                    is Outcome.Success -> editor.update {
                        it.copy(draftName = "", nameError = null, selectedId = result.value)
                    }
                    is Outcome.Failure -> editor.update { it.copy(nameError = result.error.message()) }
                }
            } catch (_: Exception) {
                editor.update { it.copy(nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onIncrement() = onSelected { incrementCounter(it) }

    fun onDecrement() = onSelected { decrementCounter(it) }

    fun onUndo() = onSelected { undoCounterChange(it) }

    fun onTargetChange(value: String) {
        editor.update { it.copy(targetInput = value, targetError = null) }
    }

    fun onSaveTarget() {
        val counterId = editor.value.selectedId ?: return
        viewModelScope.launch {
            try {
                when (val result = setCounterTarget(counterId, editor.value.targetInput)) {
                    is Outcome.Success -> editor.update { it.copy(targetError = null) }
                    is Outcome.Failure -> editor.update { it.copy(targetError = result.error.message()) }
                }
            } catch (_: Exception) {
                editor.update { it.copy(targetError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onClearTarget() = onSelected { counterId ->
        clearCounterTarget(counterId)
        editor.update { it.copy(targetInput = "", targetError = null) }
    }

    fun onSetMain() = onSelected { setMainCounter(it) }

    fun onDeleteRequested() {
        if (editor.value.selectedId != null) {
            editor.update { it.copy(confirmDelete = true) }
        }
    }

    fun onDeleteDismissed() {
        editor.update { it.copy(confirmDelete = false) }
    }

    fun onDelete() {
        val counterId = editor.value.selectedId ?: return
        viewModelScope.launch {
            try {
                deleteCounter(counterId)
                editor.update { it.copy(selectedId = null, confirmDelete = false) }
            } catch (_: Exception) {
                editor.update { it.copy(confirmDelete = false, nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    private fun onSelected(block: suspend (Long) -> Unit) {
        val counterId = editor.value.selectedId ?: return
        viewModelScope.launch {
            try {
                block(counterId)
            } catch (_: Exception) {
                editor.update { it.copy(nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    init {
        viewModelScope.launch {
            observeCounters(projectId).collect { counters ->
                editor.update { current ->
                    val selected = counters.firstOrNull { it.id == current.selectedId }
                        ?: counters.firstOrNull { it.id == routeCounterId }
                        ?: counters.firstOrNull { it.isMain }
                        ?: counters.firstOrNull()
                    val selectionChanged = selected?.id != current.selectedId
                    current.copy(
                        counters = counters,
                        selectedId = selected?.id,
                        value = selected?.value ?: 0,
                        isMain = selected?.isMain == true,
                        targetInput = if (selectionChanged) {
                            selected?.target?.toString().orEmpty()
                        } else {
                            current.targetInput
                        },
                    )
                }
            }
        }
    }
}
