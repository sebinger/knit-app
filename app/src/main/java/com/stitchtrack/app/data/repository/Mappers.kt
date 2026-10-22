package com.stitchtrack.app.data.repository

import com.stitchtrack.app.data.local.entity.CounterChangeEntity
import com.stitchtrack.app.data.local.entity.CounterEntity
import com.stitchtrack.app.data.local.entity.NoteEntity
import com.stitchtrack.app.data.local.entity.PatternFileEntity
import com.stitchtrack.app.data.local.entity.ProjectEntity
import com.stitchtrack.app.data.local.entity.TimerSessionEntity
import com.stitchtrack.app.domain.model.Counter
import com.stitchtrack.app.domain.model.CounterChange
import com.stitchtrack.app.domain.model.Note
import com.stitchtrack.app.domain.model.PatternFile
import com.stitchtrack.app.domain.model.Project
import com.stitchtrack.app.domain.model.TimerSession

internal fun ProjectEntity.toDomain(): Project = Project(
    id = id,
    name = name,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemType = itemType,
    coverUri = coverUri,
)

internal fun CounterChangeEntity.toDomain(): CounterChange = CounterChange(
    id = id,
    counterId = counterId,
    delta = delta,
    createdAt = createdAt,
)

internal fun CounterEntity.toDomain(): Counter = Counter(
    id = id,
    projectId = projectId,
    name = name,
    value = value,
    target = target,
    isMain = isMain,
    createdAt = createdAt,
)

internal fun NoteEntity.toDomain(): Note = Note(
    id = id,
    projectId = projectId,
    text = text,
    createdAt = createdAt,
)

internal fun TimerSessionEntity.toDomain(): TimerSession = TimerSession(
    id = id,
    projectId = projectId,
    startedAt = startedAt,
    endedAt = endedAt,
    pausedDuration = pausedDuration,
    pauseStartedAt = pauseStartedAt,
)

internal fun PatternFileEntity.toDomain(): PatternFile = PatternFile(
    id = id,
    projectId = projectId,
    fileName = fileName,
    uri = uri,
    lastPage = lastPage,
)
