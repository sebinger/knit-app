package com.stitchtrack.app.domain.model

data class Counter(
    val id: Long,
    val projectId: Long,
    val name: String,
    val value: Int,
    val target: Int?,
    val isMain: Boolean,
    val createdAt: Long,
)
