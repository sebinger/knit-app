package com.stitchtrack.app.feature

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stitchtrack.app.core.designsystem.StitchTrackTheme
import com.stitchtrack.app.core.util.TimeProvider
import com.stitchtrack.app.data.MemoryCounterChangeDao
import com.stitchtrack.app.data.MemoryCounterDao
import com.stitchtrack.app.data.MemoryNoteDao
import com.stitchtrack.app.data.MemoryPatternDao
import com.stitchtrack.app.data.MemoryProjectDao
import com.stitchtrack.app.data.MemoryTimerDao
import com.stitchtrack.app.data.repository.CounterRepositoryImpl
import com.stitchtrack.app.data.repository.ProjectRepositoryImpl
import com.stitchtrack.app.domain.usecase.ClearCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.CreateCounterUseCase
import com.stitchtrack.app.domain.usecase.CreateProjectUseCase
import com.stitchtrack.app.domain.usecase.DecrementCounterUseCase
import com.stitchtrack.app.domain.usecase.DeleteCounterUseCase
import com.stitchtrack.app.domain.usecase.IncrementCounterUseCase
import com.stitchtrack.app.domain.usecase.ObserveCounterChangesUseCase
import com.stitchtrack.app.domain.usecase.ObserveCountersUseCase
import com.stitchtrack.app.domain.usecase.SetCounterTargetUseCase
import com.stitchtrack.app.domain.usecase.SetMainCounterUseCase
import com.stitchtrack.app.domain.usecase.UndoCounterChangeUseCase
import com.stitchtrack.app.feature.counters.CounterContent
import com.stitchtrack.app.feature.counters.CounterUiState
import com.stitchtrack.app.feature.counters.CounterViewModel
import com.stitchtrack.app.feature.createproject.CreateProjectContent
import com.stitchtrack.app.feature.createproject.CreateProjectUiState
import com.stitchtrack.app.feature.createproject.CreateProjectViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.junit.Rule
import org.junit.Test

class CriticalFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyProjectNameShowsError() {
        val repository = ProjectRepositoryImpl(
            MemoryProjectDao(),
            MemoryCounterDao(),
            MemoryCounterChangeDao(),
            MemoryNoteDao(),
            MemoryTimerDao(),
            MemoryPatternDao(),
        )
        val viewModel = CreateProjectViewModel(CreateProjectUseCase(repository, TimeProvider { 1L }))
        composeRule.setContent {
            StitchTrackTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CreateProjectContent(
                    uiState = uiState,
                    onNameChange = viewModel::onNameChange,
                    onCreate = viewModel::onCreate,
                )
            }
        }

        composeRule.onNodeWithTag("project_create").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Enter a name.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Enter a name.").assertIsDisplayed()
    }

    @Test
    fun counterIncrementAndDecrementChangeTheShownValue() {
        val repository = CounterRepositoryImpl(MemoryCounterDao(), MemoryCounterChangeDao())
        val time = TimeProvider { 1L }
        kotlinx.coroutines.runBlocking { repository.create(projectId = 1, name = "Rows", createdAt = 1) }
        val viewModel = CounterViewModel(
            savedStateHandle = SavedStateHandle(mapOf("projectId" to 1L, "counterId" to 0L)),
            observeCounters = ObserveCountersUseCase(repository),
            observeCounterChanges = ObserveCounterChangesUseCase(repository),
            createCounter = CreateCounterUseCase(repository, time),
            incrementCounter = IncrementCounterUseCase(repository, time),
            decrementCounter = DecrementCounterUseCase(repository, time),
            undoCounterChange = UndoCounterChangeUseCase(repository),
            setCounterTarget = SetCounterTargetUseCase(repository),
            clearCounterTarget = ClearCounterTargetUseCase(repository),
            setMainCounter = SetMainCounterUseCase(repository),
            deleteCounter = DeleteCounterUseCase(repository),
        )
        composeRule.setContent {
            StitchTrackTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CounterContent(
                    uiState = uiState,
                    onBack = {},
                    onIncrement = viewModel::onIncrement,
                    onDecrement = viewModel::onDecrement,
                    onUndo = viewModel::onUndo,
                    onSetMain = viewModel::onSetMain,
                    onTargetChange = viewModel::onTargetChange,
                    onSaveTarget = viewModel::onSaveTarget,
                    onClearTarget = viewModel::onClearTarget,
                    onDelete = viewModel::onDelete,
                    onDeleteDismissed = viewModel::onDeleteDismissed,
                    onDeleteRequested = viewModel::onDeleteRequested,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("counter_value").fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodesWithText("0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("counter_increment").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("1").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("counter_decrement").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("counter_decrement").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("0").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
