package com.stitchtrack.app.feature.projects

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.stitchtrack.app.core.designsystem.StitchTrackTheme
import com.stitchtrack.app.domain.model.ProjectStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProjectsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsEmptyProjectList() {
        composeRule.setContent {
            StitchTrackTheme {
                ProjectsContent(
                    uiState = ProjectsUiState(),
                    onCreateProject = {},
                    onOpenProject = {},
                )
            }
        }

        composeRule.onNodeWithText("StitchTrack").assertIsDisplayed()
        composeRule.onNodeWithText("No projects yet").assertIsDisplayed()
        composeRule.onNodeWithText("Create project").assertIsDisplayed()
    }

    @Test
    fun newProjectInvokesCallback() {
        var opened = false
        composeRule.setContent {
            StitchTrackTheme {
                ProjectsContent(
                    uiState = ProjectsUiState(
                        projects = listOf(ProjectListItem(1, "Blue scarf", ProjectStatus.NOT_STARTED)),
                    ),
                    onCreateProject = { opened = true },
                    onOpenProject = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("New project").performClick()
        assertTrue(opened)
        composeRule.onNodeWithText("Blue scarf").assertIsDisplayed()
    }
}
