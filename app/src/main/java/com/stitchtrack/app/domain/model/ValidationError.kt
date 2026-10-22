package com.stitchtrack.app.domain.model

enum class ValidationError {
    ProjectName,
    CounterName,
    Target,
    NoteText,
    Pattern,
}

fun ValidationError.message(): String = when (this) {
    ValidationError.ProjectName -> "Enter a name."
    ValidationError.CounterName -> "Enter a counter name."
    ValidationError.Target -> "Enter a whole number greater than 0."
    ValidationError.NoteText -> "Enter the note text."
    ValidationError.Pattern -> "Choose a PDF file."
}

const val UNEXPECTED_ERROR = "Something went wrong."
