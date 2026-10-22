package com.stitchtrack.app.domain.repository

import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.ProjectStatus
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeProjects(): Flow<List<Project>>

    fun observeProject(id: Long): Flow<Project?>

    suspend fun getProject(id: Long): Project?

    suspend fun insert(
        name: String,
        status: ProjectStatus,
        createdAt: Long,
        updatedAt: Long,
    ): Long

    suspend fun update(project: Project)

    suspend fun delete(id: Long)
}
