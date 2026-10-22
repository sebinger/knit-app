package com.stitchtrack.app.domain.repository

import com.stitchtrack.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(projectId: Long): Flow<List<Note>>

    suspend fun getNote(id: Long): Note?

    suspend fun add(projectId: Long, text: String, createdAt: Long): Long

    suspend fun update(id: Long, text: String)

    suspend fun delete(id: Long)
}
