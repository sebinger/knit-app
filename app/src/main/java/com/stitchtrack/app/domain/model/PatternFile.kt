package com.stitchtrack.app.domain.model

data class PatternFile(
    val id: Long,
    val projectId: Long,
    val fileName: String,
    val uri: String,
    val lastPage: Int,
)
