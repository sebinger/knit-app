package com.stitchtrack.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CounterProgressTest {
    @Test
    fun noCounterHasNoPercentage() {
        val progress = progressOf(emptyList())
        assertNull(progress.percentage)
        assertNull(progress.fractionLabel)
    }

    @Test
    fun noTargetHasNoPercentage() {
        val progress = progressOf(listOf(counter(value = 25, target = null, isMain = true)))
        assertEquals(25, progress.value)
        assertNull(progress.percentage)
        assertNull(progress.fractionLabel)
    }

    @Test
    fun targetReachedShowsPercentageAndFraction() {
        val progress = progressOf(listOf(counter(value = 25, target = 100, isMain = true)))
        assertEquals(25, progress.percentage)
        assertEquals("25 of 100", progress.fractionLabel)
    }

    @Test
    fun overTargetCapsPercentageAndKeepsRealNumbers() {
        val progress = progressOf(listOf(counter(value = 110, target = 100, isMain = true)))
        assertEquals(100, progress.percentage)
        assertEquals("110 of 100", progress.fractionLabel)
    }

    @Test
    fun progressUsesTheMainCounter() {
        val progress = progressOf(
            listOf(
                counter(id = 1, value = 10, target = 10, isMain = false),
                counter(id = 2, value = 25, target = 100, isMain = true),
            ),
        )
        assertEquals(2L, progress.counterId)
        assertEquals(25, progress.percentage)
    }

    private fun counter(
        id: Long = 1,
        value: Int,
        target: Int?,
        isMain: Boolean,
    ) = Counter(
        id = id,
        projectId = 1,
        name = "Rows",
        value = value,
        target = target,
        isMain = isMain,
        createdAt = id,
    )
}
