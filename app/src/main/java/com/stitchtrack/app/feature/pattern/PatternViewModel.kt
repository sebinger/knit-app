package com.stitchtrack.app.feature.pattern

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stitchtrack.app.core.navigation.PatternRoute
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.usecase.AttachPatternUseCase
import com.stitchtrack.app.domain.usecase.ObservePatternUseCase
import com.stitchtrack.app.domain.usecase.RememberPatternPageUseCase
import com.stitchtrack.app.domain.usecase.RemovePatternUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatternUiState(
    val pattern: PatternFile? = null,
    val error: String? = null,
    val confirmRemove: Boolean = false,
)

@HiltViewModel
class PatternViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePattern: ObservePatternUseCase,
    private val attachPattern: AttachPatternUseCase,
    private val removePattern: RemovePatternUseCase,
    private val rememberPatternPage: RememberPatternPageUseCase,
) : ViewModel() {
    private val projectId: Long = savedStateHandle.toRoute<PatternRoute>().projectId
    private val flags = MutableStateFlow(PatternUiState())

    val uiState: StateFlow<PatternUiState> = combine(
        observePattern(projectId),
        flags,
    ) { pattern, current ->
        current.copy(pattern = pattern)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PatternUiState(),
    )

    fun onAttach(fileName: String, uri: String) {
        viewModelScope.launch {
            try {
                when (val result = attachPattern(projectId, fileName, uri)) {
                    is Outcome.Success -> flags.update { it.copy(error = null) }
                    is Outcome.Failure -> flags.update { it.copy(error = result.error.message()) }
                }
            } catch (_: Exception) {
                flags.update { it.copy(error = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onRemoveRequested() {
        flags.update { it.copy(confirmRemove = true) }
    }

    fun onRemoveDismissed() {
        flags.update { it.copy(confirmRemove = false) }
    }

    fun onRemoveConfirmed() {
        viewModelScope.launch {
            try {
                removePattern(projectId)
                flags.update { it.copy(confirmRemove = false, error = null) }
            } catch (_: Exception) {
                flags.update { it.copy(confirmRemove = false, error = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onErrorDismissed() {
        flags.update { it.copy(error = null) }
    }

    fun onPageSelected(page: Int) {
        viewModelScope.launch {
            try {
                rememberPatternPage(projectId, page)
            } catch (_: Exception) {
                flags.update { it.copy(error = UNEXPECTED_ERROR) }
            }
        }
    }
}
