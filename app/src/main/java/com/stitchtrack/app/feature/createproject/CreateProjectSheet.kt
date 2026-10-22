package com.stitchtrack.app.feature.createproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R

@Composable
fun CreateProjectSheet(
    onCreated: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.createdProjectId) {
        uiState.createdProjectId?.let(onCreated)
    }
    CreateProjectContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onCreate = viewModel::onCreate,
        modifier = modifier,
    )
}

@Composable
fun CreateProjectContent(
    uiState: CreateProjectUiState,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.create_project_title),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .testTag("project_name"),
            label = { Text(text = stringResource(R.string.field_name)) },
            placeholder = { Text(text = stringResource(R.string.name_placeholder)) },
            singleLine = true,
            isError = uiState.error != null,
            supportingText = {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        modifier = Modifier.testTag("project_name_error"),
                    )
                }
            },
        )
        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("project_create"),
        ) {
            Text(text = stringResource(R.string.action_create))
        }
    }
}
