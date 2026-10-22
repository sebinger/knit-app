package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.domain.model.ItemType
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.ProjectStatus
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
) {
    operator fun invoke(): Flow<List<Project>> = projectRepository.observeProjects()
}

class ObserveProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
) {
    operator fun invoke(projectId: Long): Flow<Project?> = projectRepository.observeProject(projectId)
}

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(rawName: String): Outcome<Long> {
        val name = rawName.trim()
        if (name.isEmpty()) return Outcome.Failure(ValidationError.ProjectName)
        val now = timeProvider.nowMillis()
        return Outcome.Success(
            projectRepository.insert(
                name = name,
                status = ProjectStatus.NOT_STARTED,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}

class RenameProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, rawName: String): Outcome<Unit> {
        val name = rawName.trim()
        if (name.isEmpty()) return Outcome.Failure(ValidationError.ProjectName)
        val project = projectRepository.getProject(projectId) ?: return Outcome.Success(Unit)
        projectRepository.update(
            project.copy(name = name, updatedAt = timeProvider.nowMillis()),
        )
        return Outcome.Success(Unit)
    }
}

class UpdateProjectStatusUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, status: ProjectStatus) {
        val project = projectRepository.getProject(projectId) ?: return
        projectRepository.update(
            project.copy(status = status, updatedAt = timeProvider.nowMillis()),
        )
    }
}

class UpdateProjectTypeUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, itemType: ItemType) {
        val project = projectRepository.getProject(projectId) ?: return
        projectRepository.update(
            project.copy(itemType = itemType, updatedAt = timeProvider.nowMillis()),
        )
    }
}

class UpdateProjectCoverUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, coverUri: String?) {
        val project = projectRepository.getProject(projectId) ?: return
        projectRepository.update(
            project.copy(coverUri = coverUri, updatedAt = timeProvider.nowMillis()),
        )
    }
}

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(projectId: Long) {
        projectRepository.delete(projectId)
    }
}
