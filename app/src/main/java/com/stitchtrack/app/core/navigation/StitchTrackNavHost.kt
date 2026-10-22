package com.stitchtrack.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.stitchtrack.app.core.ui.StitchTrackBottomSheet
import com.stitchtrack.app.feature.counters.CounterScreen
import com.stitchtrack.app.feature.createproject.CreateProjectSheet
import com.stitchtrack.app.feature.editproject.EditProjectScreen
import com.stitchtrack.app.feature.notes.NotesScreen
import com.stitchtrack.app.feature.pattern.PatternViewerScreen
import com.stitchtrack.app.feature.projectdetails.ProjectDetailsScreen
import com.stitchtrack.app.feature.projects.ProjectsScreen

@Composable
fun StitchTrackNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ProjectsRoute,
        modifier = modifier,
    ) {
        composable<ProjectsRoute> {
            ProjectsScreen(
                onCreateProject = { navController.navigate(CreateProjectRoute) },
                onOpenProject = { projectId ->
                    navController.navigate(ProjectDetailsRoute(projectId))
                },
            )
        }
        dialog<CreateProjectRoute>(
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            StitchTrackBottomSheet(
                onDismiss = { navController.popBackStack<CreateProjectRoute>(inclusive = true) },
            ) {
                CreateProjectSheet(
                    onCreated = { projectId ->
                        navController.popBackStack<CreateProjectRoute>(inclusive = true)
                        navController.navigate(ProjectDetailsRoute(projectId))
                    },
                )
            }
        }
        composable<ProjectDetailsRoute> { entry ->
            val route = entry.toRoute<ProjectDetailsRoute>()
            ProjectDetailsScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(EditProjectRoute(route.projectId)) },
                onOpenCounter = { counterId ->
                    navController.navigate(CountersRoute(route.projectId, counterId))
                },
                onNotes = { navController.navigate(NotesRoute(route.projectId)) },
                onPattern = { navController.navigate(PatternRoute(route.projectId)) },
                onDeleted = { navController.popBackStack<ProjectsRoute>(inclusive = false) },
            )
        }
        composable<EditProjectRoute> {
            EditProjectScreen(
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack<ProjectsRoute>(inclusive = false) },
            )
        }
        composable<CountersRoute> {
            CounterScreen(
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable<NotesRoute> {
            NotesScreen(onBack = { navController.popBackStack() })
        }
        composable<PatternRoute> {
            PatternViewerScreen(onBack = { navController.popBackStack() })
        }
    }
}
