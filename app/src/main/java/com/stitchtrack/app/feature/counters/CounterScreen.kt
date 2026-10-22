package com.stitchtrack.app.feature.counters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R

@Composable
fun CounterScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: CounterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CounterContent(
        uiState = uiState,
        onBack = onBack,
        onIncrement = viewModel::onIncrement,
        onDecrement = viewModel::onDecrement,
        onUndo = viewModel::onUndo,
        onSetMain = viewModel::onSetMain,
        onTargetChange = viewModel::onTargetChange,
        onSaveTarget = viewModel::onSaveTarget,
        onClearTarget = viewModel::onClearTarget,
        onDelete = {
            viewModel.onDelete()
            onDeleted()
        },
        onDeleteDismissed = viewModel::onDeleteDismissed,
        onDeleteRequested = viewModel::onDeleteRequested,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterContent(
    uiState: CounterUiState,
    onBack: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onUndo: () -> Unit,
    onSetMain: () -> Unit,
    onTargetChange: (String) -> Unit,
    onSaveTarget: () -> Unit,
    onClearTarget: () -> Unit,
    onDelete: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteRequested: () -> Unit,
) {
    val name = uiState.counters.firstOrNull { it.id == uiState.selectedId }?.name
        ?: stringResource(R.string.counters_title)
    val target = uiState.counters.firstOrNull { it.id == uiState.selectedId }?.target
    var showTarget by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(64.dp)
                            .semantics { contentDescription = "Decrease counter" }
                            .testTag("counter_decrement"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(R.string.action_decrement), style = MaterialTheme.typography.headlineMedium)
                    }
                    Button(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(64.dp)
                            .semantics { contentDescription = "Increase counter" }
                            .testTag("counter_increment"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(R.string.action_increment), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = uiState.value.toString(),
                    modifier = Modifier.testTag("counter_value"),
                    style = MaterialTheme.typography.displayLarge,
                )
                if (uiState.isMain) {
                    Text(text = stringResource(R.string.main_counter), style = MaterialTheme.typography.titleMedium)
                } else if (uiState.selectedId != null) {
                    IconButton(
                        onClick = onSetMain,
                        modifier = Modifier.semantics { contentDescription = "Set as main counter" },
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null)
                    }
                }
                if (target != null) {
                    Text(
                        text = "${uiState.value} of $target",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Row {
                    IconButton(
                        onClick = { showTarget = true },
                        modifier = Modifier.semantics { contentDescription = "Set target" },
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null)
                    }
                    IconButton(
                        onClick = onClearTarget,
                        enabled = target != null,
                        modifier = Modifier.semantics { contentDescription = "Clear target" },
                    ) {
                        Icon(Icons.Outlined.Adjust, contentDescription = null)
                    }
                    IconButton(
                        onClick = onUndo,
                        enabled = uiState.canUndo,
                        modifier = Modifier.semantics { contentDescription = uiState.undoLabel },
                    ) {
                        Icon(Icons.Filled.Undo, contentDescription = null)
                    }
                }
                uiState.targetError?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
                if (uiState.selectedId != null) {
                    TextButton(onClick = onDeleteRequested) {
                        Text(text = stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
    if (showTarget) {
        AlertDialog(
            onDismissRequest = { showTarget = false },
            title = { Text(text = stringResource(R.string.action_set_target)) },
            text = {
                OutlinedTextField(
                    value = uiState.targetInput,
                    onValueChange = onTargetChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("counter_target"),
                    label = { Text(text = stringResource(R.string.field_target)) },
                    isError = uiState.targetError != null,
                    supportingText = { uiState.targetError?.let { Text(text = it) } },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = { onSaveTarget(); showTarget = false }) {
                    Text(text = stringResource(R.string.action_set))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTarget = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }
    if (uiState.confirmDelete) {
        AlertDialog(
            onDismissRequest = onDeleteDismissed,
            title = { Text(text = stringResource(R.string.delete_counter_title)) },
            text = { Text(text = stringResource(R.string.delete_counter_body)) },
            confirmButton = {
                TextButton(onClick = onDelete) { Text(text = stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismissed) { Text(text = stringResource(R.string.action_cancel)) }
            },
        )
    }
}
