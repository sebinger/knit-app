package com.stitchtrack.app.domain.usecase

import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterChange
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.ValidationError
import com.stitchtrack.app.domain.model.parseTarget
import com.stitchtrack.app.domain.repository.CounterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCountersUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    operator fun invoke(projectId: Long): Flow<List<Counter>> = counterRepository.observeCounters(projectId)

    fun all(): Flow<List<Counter>> = counterRepository.observeAllCounters()
}

class ObserveCounterChangesUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    operator fun invoke(counterId: Long): Flow<List<CounterChange>> = counterRepository.observeChanges(counterId)
}

class CreateCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(projectId: Long, rawName: String): Outcome<Long> {
        val name = rawName.trim()
        if (name.isEmpty()) return Outcome.Failure(ValidationError.CounterName)
        return Outcome.Success(
            counterRepository.create(
                projectId = projectId,
                name = name,
                createdAt = timeProvider.nowMillis(),
            ),
        )
    }
}

class IncrementCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.increment(counterId, timeProvider.nowMillis())
    }
}

class DecrementCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.decrement(counterId, timeProvider.nowMillis())
    }
}

class UndoCounterChangeUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.undo(counterId)
    }
}

class SetCounterTargetUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long, rawTarget: String): Outcome<Unit> {
        return when (val parsed = parseTarget(rawTarget)) {
            is Outcome.Failure -> parsed
            is Outcome.Success -> {
                counterRepository.setTarget(counterId, parsed.value)
                Outcome.Success(Unit)
            }
        }
    }
}

class ClearCounterTargetUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.clearTarget(counterId)
    }
}

class SetMainCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.setMain(counterId)
    }
}

class DeleteCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long) {
        counterRepository.delete(counterId)
    }
}
