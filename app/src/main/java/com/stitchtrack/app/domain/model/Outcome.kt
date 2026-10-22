package com.stitchtrack.app.domain.model

sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>

    data class Failure(val error: ValidationError) : Outcome<Nothing>
}
