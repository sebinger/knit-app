package com.stitchtrack.app.data.repository

import com.stitchtrack.app.data.local.entity.ProjectEntity
import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.ProjectStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectMapperTest {
    @Test
    fun mapsStoredProjectToDomain() {
        val entity = ProjectEntity(
            id = 4,
            name = "Hat",
            status = ProjectStatus.PAUSED,
            createdAt = 1,
            updatedAt = 2,
        )

        assertEquals(
            Project(
                id = 4,
                name = "Hat",
                status = ProjectStatus.PAUSED,
                createdAt = 1,
                updatedAt = 2,
            ),
            entity.toDomain(),
        )
    }
}
