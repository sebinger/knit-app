package com.stitchtrack.app.feature.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stitchtrack.app.core.navigation.NotesRoute
import com.stitchtrack.app.domain.model.Note
import com.stitchtrack.app.domain.model.Outcome
import com.stitchtrack.app.domain.model.UNEXPECTED_ERROR
import com.stitchtrack.app.domain.model.message
import com.stitchtrack.app.domain.usecase.AddNoteUseCase
import com.stitchtrack.app.domain.usecase.DeleteNoteUseCase
import com.stitchtrack.app.domain.usecase.ObserveNotesUseCase
import com.stitchtrack.app.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val draft: String = "",
    val editingId: Long? = null,
    val error: String? = null,
    val pendingDeleteId: Long? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeNotes: ObserveNotesUseCase,
    private val addNote: AddNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
) : ViewModel() {
    private val projectId: Long = savedStateHandle.toRoute<NotesRoute>().projectId
    private val editor = MutableStateFlow(NotesUiState())

    val uiState: StateFlow<NotesUiState> = combine(
        observeNotes(projectId),
        editor,
    ) { notes, current ->
        current.copy(notes = notes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(),
    )

    fun onDraftChange(text: String) {
        editor.update { it.copy(draft = text, error = null) }
    }

    fun onEdit(note: Note) {
        editor.update { it.copy(editingId = note.id, draft = note.text, error = null) }
    }

    fun onCancelEdit() {
        editor.update { it.copy(editingId = null, draft = "", error = null) }
    }

    fun onSave() {
        val current = editor.value
        viewModelScope.launch {
            try {
                val result = if (current.editingId == null) {
                    addNote(projectId, current.draft)
                } else {
                    updateNote(current.editingId, current.draft)
                }
                when (result) {
                    is Outcome.Success -> editor.update {
                        it.copy(draft = "", editingId = null, error = null, saved = true)
                    }
                    is Outcome.Failure -> editor.update { it.copy(error = result.error.message()) }
                }
            } catch (_: Exception) {
                editor.update { it.copy(error = UNEXPECTED_ERROR) }
            }
        }
    }

    fun onSavedConsumed() {
        editor.update { it.copy(saved = false) }
    }

    fun onDeleteRequested(noteId: Long) {
        editor.update { it.copy(pendingDeleteId = noteId) }
    }

    fun onDeleteDismissed() {
        editor.update { it.copy(pendingDeleteId = null) }
    }

    fun onDeleteConfirmed() {
        val noteId = editor.value.pendingDeleteId ?: return
        viewModelScope.launch {
            try {
                deleteNote(noteId)
                editor.update {
                    it.copy(
                        pendingDeleteId = null,
                        editingId = if (it.editingId == noteId) null else it.editingId,
                        draft = if (it.editingId == noteId) "" else it.draft,
                    )
                }
            } catch (_: Exception) {
                editor.update { it.copy(pendingDeleteId = null, error = UNEXPECTED_ERROR) }
            }
        }
    }
}
