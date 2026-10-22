package com.stitchtrack.app.feature.editproject

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R
import com.stitchtrack.app.core.ui.CoverThumbnail
import com.stitchtrack.app.core.ui.StatusTag
import com.stitchtrack.app.core.util.takePersistableReadPermission
import com.stitchtrack.app.domain.model.ItemType
import com.stitchtrack.app.domain.model.ProjectStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: EditProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        takePersistableReadPermission(context, uri)
        viewModel.onCoverChange(uri.toString())
    }
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(text = stringResource(R.string.action_cancel)) }
                },
                actions = {
                    TextButton(onClick = viewModel::onSave) { Text(text = stringResource(R.string.action_save)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.field_name)) },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = { uiState.nameError?.let { Text(text = it) } },
            )
            SelectionRow(
                label = stringResource(R.string.field_type),
                value = uiState.itemType.label,
                onClick = { viewModel.onTypePicker(true) },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (uiState.coverUri != null) {
                    CoverThumbnail(coverUri = uiState.coverUri, size = 96.dp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { coverPicker.launch(arrayOf("image/*")) }) {
                        Text(
                            text = stringResource(
                                if (uiState.coverUri == null) R.string.action_add_cover else R.string.action_replace_cover,
                            ),
                        )
                    }
                    if (uiState.coverUri != null) {
                        TextButton(onClick = { viewModel.onCoverChange(null) }) {
                            Text(text = stringResource(R.string.action_remove_cover))
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onStatusPicker(true) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.field_status),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                StatusTag(status = uiState.status)
            }
            TextButton(
                onClick = viewModel::onDeleteRequested,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_delete_project),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    if (uiState.showTypePicker) {
        ChoiceDialog(
            title = stringResource(R.string.field_type),
            onDismiss = { viewModel.onTypePicker(false) },
        ) {
            ItemType.entries.forEach { type ->
                TextButton(onClick = { viewModel.onTypeChange(type) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = type.label)
                }
            }
        }
    }
    if (uiState.showStatusPicker) {
        ChoiceDialog(
            title = stringResource(R.string.field_status),
            onDismiss = { viewModel.onStatusPicker(false) },
        ) {
            ProjectStatus.entries.forEach { status ->
                TextButton(
                    onClick = { viewModel.onStatusChange(status) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StatusTag(status = status)
                }
            }
        }
    }
    if (uiState.confirmDelete) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(text = stringResource(R.string.delete_title)) },
            text = { Text(text = stringResource(R.string.delete_body)) },
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
private fun SelectionRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(text = value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Column { content() } },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel)) }
        },
    )
}
