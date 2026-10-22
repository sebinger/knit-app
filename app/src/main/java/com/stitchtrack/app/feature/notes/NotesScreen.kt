package com.stitchtrack.app.feature.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R
import com.stitchtrack.app.domain.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editorOpen by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            editorOpen = false
            viewModel.onSavedConsumed()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.notes_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onCancelEdit()
                            editorOpen = true
                        },
                        modifier = Modifier.semantics { contentDescription = "Add note" },
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = stringResource(R.string.notes_empty_title), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = stringResource(R.string.notes_empty_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.notes, key = { it.id }) { note ->
                    NoteRow(
                        note = note,
                        onEdit = {
                            viewModel.onEdit(note)
                            editorOpen = true
                        },
                        onDelete = { viewModel.onDeleteRequested(note.id) },
                    )
                }
            }
        }
    }
    if (editorOpen) {
        AlertDialog(
            onDismissRequest = { editorOpen = false; viewModel.onCancelEdit() },
            title = { Text(text = stringResource(R.string.field_note)) },
            text = {
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = viewModel::onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_text"),
                    label = { Text(text = stringResource(R.string.field_note)) },
                    isError = uiState.error != null,
                    supportingText = { uiState.error?.let { Text(text = it) } },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onSave,
                    modifier = Modifier.testTag("note_save"),
                ) {
                    Text(text = stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false; viewModel.onCancelEdit() }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }
    uiState.pendingDeleteId?.let {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(text = stringResource(R.string.delete_note_title)) },
            text = { Text(text = stringResource(R.string.delete_note_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirmed) {
                    Text(text = stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismissed) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun NoteRow(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember(note.id) { mutableStateOf(false) }
    RowNote(note.text, menu, { menu = it }, onEdit, onDelete)
}

@Composable
private fun RowNote(
    text: String,
    menu: Boolean,
    onMenu: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        IconButton(
            onClick = { onMenu(true) },
            modifier = Modifier.semantics { contentDescription = "Note actions" },
        ) {
            Icon(Icons.Filled.MoreVert, contentDescription = null)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { onMenu(false) }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.action_edit_note)) },
                onClick = { onMenu(false); onEdit() },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.action_delete)) },
                onClick = { onMenu(false); onDelete() },
            )
        }
    }
}
