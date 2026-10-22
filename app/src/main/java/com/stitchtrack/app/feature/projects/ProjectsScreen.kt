package com.stitchtrack.app.feature.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R
import com.stitchtrack.app.core.designsystem.StitchTrackTheme
import com.stitchtrack.app.core.ui.StatusTag
import com.stitchtrack.app.domain.model.ProjectStatus

@Composable
fun ProjectsScreen(
    onCreateProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    viewModel: ProjectsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProjectsContent(
        uiState = uiState,
        onCreateProject = onCreateProject,
        onOpenProject = onOpenProject,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsContent(
    uiState: ProjectsUiState,
    onCreateProject: () -> Unit,
    onOpenProject: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val newProjectLabel = stringResource(R.string.action_new_project)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = onCreateProject,
                        modifier = Modifier
                            .semantics { contentDescription = newProjectLabel }
                            .testTag("new_project"),
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isEmpty) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.projects_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.projects_empty_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onCreateProject,
                    modifier = Modifier.testTag("create_project_empty"),
                ) {
                    Text(text = stringResource(R.string.action_create_project))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.projects, key = { it.id }) { project ->
                    ProjectCard(project = project, onClick = { onOpenProject(project.id) })
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectListItem,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_${project.id}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = project.name, style = MaterialTheme.typography.titleLarge)
            StatusTag(status = project.status)
            project.percentage?.let { percentage ->
                Text(
                    text = stringResource(R.string.progress_percent, percentage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectsContentPreview() {
    StitchTrackTheme {
        ProjectsContent(
            uiState = ProjectsUiState(
                projects = listOf(
                    ProjectListItem(1, "Blue scarf", ProjectStatus.IN_PROGRESS, 25),
                ),
            ),
            onCreateProject = {},
            onOpenProject = {},
        )
    }
}
