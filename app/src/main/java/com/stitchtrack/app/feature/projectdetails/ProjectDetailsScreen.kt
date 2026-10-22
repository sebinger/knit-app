package com.stitchtrack.app.feature.projectdetails

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R
import com.stitchtrack.app.core.ui.CoverThumbnail
import com.stitchtrack.app.core.ui.StatusTag
import com.stitchtrack.app.core.util.displayName
import com.stitchtrack.app.core.util.takePersistableReadPermission
import com.stitchtrack.app.domain.model.Counter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenCounter: (Long) -> Unit,
    onNotes: () -> Unit,
    onPattern: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ProjectDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val projectId = uiState.project?.id
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        takePersistableReadPermission(context, uri)
        viewModel.onAttachPattern(displayName(context, uri), uri.toString())
    }
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }
    LaunchedEffect(uiState.createdCounterId) {
        val counterId = uiState.createdCounterId ?: return@LaunchedEffect
        onOpenCounter(counterId)
        viewModel.onCreatedCounterConsumed()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.project?.name ?: stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (projectId != null) {
                        IconButton(
                            onClick = { onEdit(projectId) },
                            modifier = Modifier.semantics {
                                contentDescription = context.getString(R.string.action_edit)
                            },
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                    }
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Header(uiState)
            ProgressSection(uiState, onAddCounter = viewModel::onAddCounterOpened)
            TimerSection(uiState, onStart = viewModel::onStart, onPause = viewModel::onPause, onStop = viewModel::onStop)
            PatternSection(
                fileName = uiState.pattern?.fileName,
                onOpen = onPattern,
                onAttach = { picker.launch(arrayOf("application/pdf")) },
                onReplace = { picker.launch(arrayOf("application/pdf")) },
                onRemove = viewModel::onRemovePatternRequested,
            )
            CountersSection(
                counters = uiState.counters,
                onOpen = onOpenCounter,
                onAdd = viewModel::onAddCounterOpened,
                onDelete = viewModel::onDeleteCounterRequested,
            )
            NotesRow(count = uiState.notes.size, onClick = onNotes)
        }
    }
    if (uiState.showAddCounter) {
        ModalBottomSheet(onDismissRequest = viewModel::onAddCounterDismissed) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(text = stringResource(R.string.action_add_counter), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = uiState.counterDraft,
                    onValueChange = viewModel::onCounterDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text(text = stringResource(R.string.field_name)) },
                    isError = uiState.counterError != null,
                    supportingText = {
                        uiState.counterError?.let { Text(text = it) }
                    },
                    singleLine = true,
                )
                Button(
                    onClick = viewModel::onCreateCounter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                ) {
                    Text(text = stringResource(R.string.action_create))
                }
            }
        }
    }
    if (uiState.confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.delete_title),
            body = stringResource(R.string.delete_body),
            confirm = stringResource(R.string.action_delete),
            onDismiss = viewModel::onDeleteDismissed,
            onConfirm = viewModel::onDeleteConfirmed,
        )
    }
    uiState.confirmDeleteCounterId?.let {
        ConfirmDialog(
            title = stringResource(R.string.delete_counter_title),
            body = stringResource(R.string.delete_counter_body),
            confirm = stringResource(R.string.action_delete),
            onDismiss = viewModel::onDeleteCounterDismissed,
            onConfirm = viewModel::onDeleteCounterConfirmed,
        )
    }
    if (uiState.confirmRemovePattern) {
        ConfirmDialog(
            title = stringResource(R.string.remove_pattern_title),
            body = stringResource(R.string.remove_pattern_body),
            confirm = stringResource(R.string.action_remove),
            onDismiss = viewModel::onRemovePatternDismissed,
            onConfirm = viewModel::onRemovePatternConfirmed,
        )
    }
    uiState.patternError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onPatternErrorDismissed,
            title = { Text(text = stringResource(R.string.pattern_error_title)) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = viewModel::onPatternErrorDismissed) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun Header(uiState: ProjectDetailsUiState) {
    val project = uiState.project ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (project.coverUri != null) {
            CoverThumbnail(coverUri = project.coverUri)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = project.itemType.label, style = MaterialTheme.typography.titleMedium)
            StatusTag(status = project.status)
        }
    }
}

@Composable
private fun ProgressSection(
    uiState: ProjectDetailsUiState,
    onAddCounter: () -> Unit,
) {
    val progress = uiState.progress
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            progress.counterId == null -> {
                Text(text = stringResource(R.string.progress_no_counter), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = onAddCounter) {
                    Text(text = stringResource(R.string.action_add_counter))
                }
            }
            progress.percentage == null -> {
                Text(
                    text = progress.value?.toString().orEmpty(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = stringResource(R.string.progress_no_target),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.progress_percent, progress.percentage),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                progress.fractionLabel?.let { fraction ->
                    Text(
                        text = fraction,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                val mainName = uiState.counters.firstOrNull { it.isMain }?.name
                if (mainName != null) {
                    Text(text = stringResource(R.string.main_counter_named, mainName))
                }
                LinearProgressIndicator(
                    progress = { progress.percentage / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (progress.percentage == 100) {
                    Text(text = stringResource(R.string.target_reached))
                }
            }
        }
    }
}

@Composable
private fun TimerSection(
    uiState: ProjectDetailsUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.timer_session), style = MaterialTheme.typography.titleMedium)
        Text(text = uiState.currentSessionLabel, style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(R.string.timer_total), style = MaterialTheme.typography.titleMedium)
        Text(text = uiState.timerLabel, style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!uiState.isTimerRunning) {
                Button(onClick = onStart) { Text(text = stringResource(R.string.action_start)) }
            }
            if (uiState.isTimerRunning) {
                Button(onClick = onPause) { Text(text = stringResource(R.string.action_pause)) }
            }
            if (uiState.isTimerRunning || uiState.isTimerPaused) {
                Button(onClick = onStop) { Text(text = stringResource(R.string.action_stop)) }
            }
        }
    }
}

@Composable
private fun PatternSection(
    fileName: String?,
    onOpen: () -> Unit,
    onAttach: () -> Unit,
    onReplace: () -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(
                text = fileName ?: stringResource(R.string.pattern_empty),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (fileName == null) {
            Button(onClick = onAttach) { Text(text = stringResource(R.string.action_attach_pdf)) }
        } else {
            Button(onClick = onOpen) { Text(text = stringResource(R.string.action_open)) }
            IconButton(
                onClick = { menu = true },
                modifier = Modifier.semantics { contentDescription = "Pattern actions" },
            ) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.action_replace_pdf)) },
                    onClick = { menu = false; onReplace() },
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.action_remove)) },
                    onClick = { menu = false; onRemove() },
                )
            }
        }
    }
}

@Composable
private fun CountersSection(
    counters: List<Counter>,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.counters_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onAdd,
                modifier = Modifier.semantics { contentDescription = "Add counter" },
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
        counters.forEach { counter ->
            var menu by remember(counter.id) { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(text = counter.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = counter.value.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_delete))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.action_delete)) },
                        onClick = { menu = false; onDelete(counter.id) },
                    )
                }
                IconButton(
                    onClick = { onOpen(counter.id) },
                    modifier = Modifier.semantics { contentDescription = "Open counter" },
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun NotesRow(count: Int, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(text = stringResource(R.string.notes_title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.notes_count, count),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier.semantics { contentDescription = "Open notes" },
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirm: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = confirm) } },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.action_cancel)) }
        },
    )
}
