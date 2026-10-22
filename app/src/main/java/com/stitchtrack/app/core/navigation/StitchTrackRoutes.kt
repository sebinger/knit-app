package com.stitchtrack.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ProjectsRoute

@Serializable
data object CreateProjectRoute

@Serializable
data class ProjectDetailsRoute(val projectId: Long)

@Serializable
data class EditProjectRoute(val projectId: Long)

@Serializable
data class CountersRoute(
    val projectId: Long,
    val counterId: Long = 0,
)

@Serializable
data class NotesRoute(val projectId: Long)

@Serializable
data class PatternRoute(val projectId: Long)
