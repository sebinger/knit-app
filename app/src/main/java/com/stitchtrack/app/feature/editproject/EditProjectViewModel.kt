package com.stitchtrack.app.feature.editproject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stitchtrack.app.core.navigation.EditProjectRoute
import com.stitchtrack.app.domain.model.ItemType
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.usecase.DeleteProjectUseCase
import com.stitchtrack.app.domain.usecase.ObserveProjectUseCase
import com.stitchtrack.app.domain.usecase.RenameProjectUseCase
import com.stitchtrack.app.domain.usecase.UpdateProjectCoverUseCase
import com.stitchtrack.app.domain.usecase.UpdateProjectStatusUseCase
import com.stitchtrack.app.domain.usecase.UpdateProjectTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProjectUiState(
    val name: String = "",
    val status: ProjectStatus = ProjectStatus.NOT_STARTED,
    val itemType: ItemType = ItemType.OTHER,
    val coverUri: String? = null,
    val showTypePicker: Boolean = false,
    val showStatusPicker: Boolean = false,
    val nameError: String? = null,
    val confirmDelete: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val loaded: Boolean = false,
)

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeProject: ObserveProjectUseCase,
    private val renameProject: RenameProjectUseCase,
    private val updateProjectStatus: UpdateProjectStatusUseCase,
    private val updateProjectType: UpdateProjectTypeUseCase,
    private val updateProjectCover: UpdateProjectCoverUseCase,
    private val deleteProject: DeleteProjectUseCase,
) : ViewModel() {
    private val projectId: Long = savedStateHandle.toRoute<EditProjectRoute>().projectId
    private val _uiState = MutableStateFlow(EditProjectUiState())
    val uiState: StateFlow<EditProjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeProject(projectId).collect { project ->
                if (project == null) return@collect
                _uiState.update { current ->
                    if (current.loaded) {
                        current.copy(
                            status = project.status,
                            itemType = project.itemType,
                            coverUri = project.coverUri,
                        )
                    } else {
                        current.copy(
                            name = project.name,
                            status = project.status,
                            itemType = project.itemType,
                            coverUri = project.coverUri,
                            loaded = true,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null, saved = false) }
    }

    fun onTypePicker(open: Boolean) {
        _uiState.update { it.copy(showTypePicker = open) }
    }

    fun onTypeChange(itemType: ItemType) {
        viewModelScope.launch {
            try {
                updateProjectType(projectId, itemType)
                _uiState.update { it.copy(itemType = itemType, showTypePicker = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(showTypePicker = false, nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onStatusPicker(open: Boolean) {
        _uiState.update { it.copy(showStatusPicker = open) }
    }

    fun onCoverChange(coverUri: String?) {
        viewModelScope.launch {
            try {
                updateProjectCover(projectId, coverUri)
                _uiState.update { it.copy(coverUri = coverUri) }
            } catch (_: Exception) {
                _uiState.update { it.copy(nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onStatusChange(status: ProjectStatus) {
        viewModelScope.launch {
            try {
                updateProjectStatus(projectId, status)
                _uiState.update { it.copy(status = status, showStatusPicker = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onSave() {
        viewModelScope.launch {
            try {
                when (val result = renameProject(projectId, _uiState.value.name)) {
                    is Outcome.Success -> _uiState.update { it.copy(nameError = null, saved = true) }
                    is Outcome.Failure -> _uiState.update { it.copy(nameError = result.error.message(), saved = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(nameError = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onDeleteRequested() {
        _uiState.update { it.copy(confirmDelete = true) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(confirmDelete = false) }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                deleteProject(projectId)
                _uiState.update { it.copy(confirmDelete = false, deleted = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(confirmDelete = false, nameError = UNEXPECTED_ERROR) }
            }
        }
    }
}
