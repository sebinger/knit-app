package com.stitchtrack.app.domain.model

data class Note(
    val id: Long,
    val projectId: Long,
    val text: String,
    val createdAt: Long,
)
