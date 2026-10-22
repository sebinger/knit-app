package com.stitchtrack.app.domain.model

data class CounterProgress(
    val counterId: Long?,
    val value: Int?,
    val target: Int?,
    val percentage: Int?,
    val fractionLabel: String?,
)

fun progressOf(counters: List<Counter>): CounterProgress {
    val main = counters.firstOrNull { it.isMain } ?: counters.minByOrNull { it.createdAt }
    if (main == null) {
        return CounterProgress(
            counterId = null,
            value = null,
            target = null,
            percentage = null,
            fractionLabel = null,
        )
    }
    val target = main.target
    if (target == null || target <= 0) {
        return CounterProgress(
            counterId = main.id,
            value = main.value,
            target = null,
            percentage = null,
            fractionLabel = null,
        )
    }
    val percentage = if (main.value >= target) 100 else (main.value * 100) / target
    return CounterProgress(
        counterId = main.id,
        value = main.value,
        target = target,
        percentage = percentage,
        fractionLabel = "${main.value} of $target",
    )
}

fun parseTarget(raw: String): Outcome<Int> {
    val number = raw.trim().toIntOrNull() ?: return Outcome.Failure(ValidationError.Target)
    if (number <= 0) return Outcome.Failure(ValidationError.Target)
    return Outcome.Success(number)
}

fun isPdfFileName(fileName: String): Boolean =
    fileName.substringAfterLast('.', "").equals("pdf", ignoreCase = true)
