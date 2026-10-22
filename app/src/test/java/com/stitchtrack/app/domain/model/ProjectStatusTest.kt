package com.stitchtrack.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectStatusTest {
    @Test
    fun labelsMatchTheProductNames() {
        assertEquals("Not Started", ProjectStatus.NOT_STARTED.label)
        assertEquals("In Progress", ProjectStatus.IN_PROGRESS.label)
        assertEquals("Completed", ProjectStatus.COMPLETED.label)
        assertEquals("Paused", ProjectStatus.PAUSED.label)
    }
}
