package com.stitchtrack.app.domain.model

data class Project(
    val id: Long,
    val name: String,
    val status: ProjectStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val itemType: ItemType = ItemType.OTHER,
    val coverUri: String? = null,
)
