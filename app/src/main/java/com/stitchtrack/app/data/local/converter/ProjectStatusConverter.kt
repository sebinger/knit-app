package com.stitchtrack.app.data.local.converter

import androidx.room.TypeConverter
import com.stitchtrack.app.domain.model.ItemType
import com.stitchtrack.app.domain.model.ProjectStatus

class ProjectStatusConverter {
    @TypeConverter
    fun toStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(value: ProjectStatus): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)

    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name
}
