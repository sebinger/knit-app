package com.stitchtrack.app.data.repository

import com.stitchtrack.app.data.MemoryCounterChangeDao
import com.stitchtrack.app.data.MemoryCounterDao
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.model.parseTarget
import com.stitchtrack.app.domain.usecase.ClearCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.CreateCounterUseCase
import com.stitchtrack.app.domain.usecase.DecrementCounterUseCase
import com.stitchtrack.app.domain.usecase.IncrementCounterUseCase
import com.stitchtrack.app.domain.usecase.SetCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.SetMainCounterUseCase
import com.stitchtrack.app.domain.usecase.UndoCounterChangeUseCase
import com.stitchtrack.app.core.util.TimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterRepositoryTest {
    private val counters = MemoryCounterDao()
    private val changes = MemoryCounterChangeDao()
    private val repository = CounterRepositoryImpl(counters, changes)
    private val time = TimeProvider { 10L }
    private val create = CreateCounterUseCase(repository, time)
    private val increment = IncrementCounterUseCase(repository, time)
    private val decrement = DecrementCounterUseCase(repository, time)
    private val undo = UndoCounterChangeUseCase(repository)
    private val setTarget = SetCounterTargetUseCase(repository)
    private val clearTarget = ClearCounterTargetUseCase(repository)
    private val setMain = SetMainCounterUseCase(repository)

    @Test
    fun incrementAddsOneAndRecordsPlusDelta() = runTest {
        val id = seed(value = 5)
        increment(id)
        assertEquals(6, repository.getCounter(id)?.value)
        assertEquals(1, changes.latest(id)?.delta)
    }

    @Test
    fun decrementSubtractsOne() = runTest {
        val id = seed(value = 6)
        decrement(id)
        assertEquals(5, repository.getCounter(id)?.value)
        assertEquals(-1, changes.latest(id)?.delta)
    }

    @Test
    fun decrementAtZeroDoesNotChangeOrRecord() = runTest {
        val id = seed(value = 0)
        decrement(id)
        assertEquals(0, repository.getCounter(id)?.value)
        assertNull(changes.latest(id))
    }

    @Test
    fun undoRestoresOnlyTheLastChangeOfThatCounter() = runTest {
        val first = seed(value = 6, name = "Rows")
        val second = (create(1, "Stitches") as Outcome.Success).value
        increment(first)
        increment(second)
        undo(first)
        assertEquals(6, repository.getCounter(first)?.value)
        assertEquals(1, repository.getCounter(second)?.value)
        undo(first)
        assertEquals(6, repository.getCounter(first)?.value)
    }

    @Test
    fun targetValidationAndClear() = runTest {
        val id = seed(value = 1, target = 10)
        assertTrue(setTarget(id, "0") is Outcome.Failure)
        assertTrue(setTarget(id, "abc") is Outcome.Failure)
        assertEquals(10, repository.getCounter(id)?.target)
        assertTrue(setTarget(id, "100") is Outcome.Success)
        assertEquals(100, repository.getCounter(id)?.target)
        clearTarget(id)
        assertNull(repository.getCounter(id)?.target)
        assertEquals(ValidationError.Target, (parseTarget("-3") as Outcome.Failure).error)
    }

    @Test
    fun firstCounterIsMainAndOnlyOneMainRemains() = runTest {
        val first = (create(1, "Rows") as Outcome.Success).value
        val second = (create(1, "Stitches") as Outcome.Success).value
        assertTrue(repository.getCounter(first)?.isMain == true)
        assertFalse(repository.getCounter(second)?.isMain == true)
        setMain(second)
        assertFalse(repository.getCounter(first)?.isMain == true)
        assertTrue(repository.getCounter(second)?.isMain == true)
    }

    @Test
    fun deletingCounterRemovesItsChanges() = runTest {
        val id = seed(value = 1)
        increment(id)
        repository.delete(id)
        assertNull(repository.getCounter(id))
        assertNull(changes.latest(id))
    }

    private suspend fun seed(value: Int, name: String = "Rows", target: Int? = null): Long {
        val id = (create(1, name) as Outcome.Success).value
        repeat(value) { increment(id) }
        changes.latest(id)?.let { changes.deleteById(it.id) }
        repeat(value - 1) { changes.deleteById(changes.latest(id)!!.id) }
        if (target != null) repository.setTarget(id, target)
        val current = counters.getById(id)!!
        counters.update(current.copy(value = value))
        while (changes.latest(id) != null) {
            changes.deleteById(changes.latest(id)!!.id)
        }
        return id
    }
}
