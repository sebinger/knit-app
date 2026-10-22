package com.stitchtrack.app.domain.model

data class CounterChange(
    val id: Long,
    val counterId: Long,
    val delta: Int,
    val createdAt: Long,
)
