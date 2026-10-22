package com.stitchtrack.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stitchtrack.app.domain.model.ItemType
import com.stitchtrack.app.domain.model.ProjectStatus

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: ProjectStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val itemType: ItemType = ItemType.OTHER,
    val coverUri: String? = null,
)
