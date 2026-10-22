package com.stitchtrack.app.feature.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.progressOf
import com.stitchtrack.app.domain.usecase.ObserveCountersUseCase
import com.stitchtrack.app.domain.usecase.ObserveProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProjectListItem(
    val id: Long,
    val name: String,
    val status: ProjectStatus,
    val percentage: Int? = null,
)

data class ProjectsUiState(
    val projects: List<ProjectListItem> = emptyList(),
) {
    val isEmpty: Boolean get() = projects.isEmpty()
}

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    observeProjects: ObserveProjectsUseCase,
    observeCounters: ObserveCountersUseCase,
) : ViewModel() {
    val uiState: StateFlow<ProjectsUiState> = combine(
        observeProjects(),
        observeCounters.all(),
    ) { projects, counters ->
        ProjectsUiState(
            projects = projects.map { project ->
                val progress = progressOf(counters.filter { it.projectId == project.id })
                ProjectListItem(
                    id = project.id,
                    name = project.name,
                    status = project.status,
                    percentage = progress.percentage,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectsUiState(),
    )
}
