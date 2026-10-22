package com.stitchtrack.app.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stitchtrack.app.domain.model.ProjectStatus

@Composable
fun StatusTag(
    status: ProjectStatus,
    modifier: Modifier = Modifier,
) {
    val container = when (status) {
        ProjectStatus.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant
        ProjectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
        ProjectStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
        ProjectStatus.PAUSED -> MaterialTheme.colorScheme.secondaryContainer
    }
    val content = when (status) {
        ProjectStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
        ProjectStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
        ProjectStatus.COMPLETED -> MaterialTheme.colorScheme.onTertiaryContainer
        ProjectStatus.PAUSED -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        modifier = modifier,
        color = container,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = content,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
