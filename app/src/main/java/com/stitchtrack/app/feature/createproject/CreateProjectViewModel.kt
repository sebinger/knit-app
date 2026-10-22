package com.stitchtrack.app.feature.createproject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.usecase.CreateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateProjectUiState(
    val name: String = "",
    val error: String? = null,
    val createdProjectId: Long? = null,
)

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val createProject: CreateProjectUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onCreate() {
        viewModelScope.launch {
            try {
                when (val result = createProject(_uiState.value.name)) {
                    is Outcome.Success -> _uiState.update {
                        it.copy(error = null, createdProjectId = result.value)
                    }
                    is Outcome.Failure -> _uiState.update {
                        it.copy(error = result.error.message())
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = UNEXPECTED_ERROR) }
            }
        }
    }
}
