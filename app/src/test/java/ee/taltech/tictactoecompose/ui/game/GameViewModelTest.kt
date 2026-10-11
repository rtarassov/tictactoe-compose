package ee.taltech.tictactoecompose.ui.game

import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.FakeGameStatsRepository
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val statsRepository = FakeGameStatsRepository()
    private var fakeTimeMs = 10_000L

    private fun createViewModel() = GameViewModel(
        statsRepository = statsRepository,
        currentTimeMs = { fakeTimeMs },
        aiMoveDelayMs = 400L,
    )

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun newViewModel_startsWithEmptyBoardAndXTurn() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.board.flatten().all { cell -> cell == null })
        assertEquals(Player.X, state.currentPlayer)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
        assertNull(state.gameStartTimeMs)
    }

    @Test
    fun onCellClick_placesSymbolAndStartsTimer() {
        val viewModel = createViewModel()

        viewModel.onCellClick(1, 1)

        val state = viewModel.uiState.value
        assertEquals(Player.X, state.board[1][1])
        assertEquals(Player.O, state.currentPlayer)
        assertEquals(10_000L, state.gameStartTimeMs)
    }

    @Test
    fun twoPlayerWin_showsWinnerAndRecordsStatistics() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onCellClick(0, 0)
        viewModel.onCellClick(1, 0)
        viewModel.onCellClick(0, 1)
        viewModel.onCellClick(1, 1)
        fakeTimeMs = 15_000L
        viewModel.onCellClick(0, 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(GameStatus.WON, state.status)
        assertEquals(Player.X, state.winner)
        assertEquals(listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)), state.winningCells)
        assertEquals(5_000L, state.finishedGameDurationMs)
        assertEquals(listOf(Triple(Player.X, false, 5_000L)), statsRepository.recordedGames)
    }

    @Test
    fun onCellClick_onOccupiedCell_changesNothing() {
        val viewModel = createViewModel()
        viewModel.onCellClick(0, 0)

        viewModel.onCellClick(0, 0)

        val state = viewModel.uiState.value
        assertEquals(Player.X, state.board[0][0])
        assertEquals(Player.O, state.currentPlayer)
        assertEquals(1, state.moveCount)
    }

    @Test
    fun vsAi_aiAnswersAfterDelayAndTapsAreIgnoredMeanwhile() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onVsAiSelectedChange(true)
        viewModel.onNewGameClick()

        viewModel.onCellClick(1, 1)
        viewModel.onCellClick(0, 0)
        assertNull(viewModel.uiState.value.board[0][0])
        assertEquals(1, viewModel.uiState.value.moveCount)

        advanceTimeBy(500L)

        val state = viewModel.uiState.value
        assertEquals(2, state.moveCount)
        assertEquals(Player.X, state.currentPlayer)
        assertEquals(1, state.board.flatten().count { cell -> cell == Player.O })
    }

    @Test
    fun vsAi_whenOStarts_aiOpensTheGame() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onVsAiSelectedChange(true)
        viewModel.onStartingPlayerSelected(Player.O)

        viewModel.onNewGameClick()
        advanceTimeBy(500L)

        val state = viewModel.uiState.value
        assertEquals(1, state.moveCount)
        assertEquals(Player.X, state.currentPlayer)
        assertTrue(state.isVsAi)
    }

    @Test
    fun newGameMidGame_asksForConfirmation_andConfirmClearsWithoutRecording() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onCellClick(0, 0)

        viewModel.onNewGameClick()
        assertTrue(viewModel.uiState.value.isNewGameConfirmationShown)
        assertEquals(Player.X, viewModel.uiState.value.board[0][0])

        viewModel.onNewGameConfirmed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isNewGameConfirmationShown)
        assertNull(state.board[0][0])
        assertNull(state.gameStartTimeMs)
        assertTrue(statsRepository.recordedGames.isEmpty())
    }

    @Test
    fun newGameMidGame_dismiss_keepsTheGame() {
        val viewModel = createViewModel()
        viewModel.onCellClick(0, 0)
        viewModel.onNewGameClick()

        viewModel.onNewGameDismissed()

        val state = viewModel.uiState.value
        assertFalse(state.isNewGameConfirmationShown)
        assertEquals(Player.X, state.board[0][0])
    }

    @Test
    fun newGameAfterWin_startsImmediatelyWithSelectedStarter() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onCellClick(0, 0)
        viewModel.onCellClick(1, 0)
        viewModel.onCellClick(0, 1)
        viewModel.onCellClick(1, 1)
        viewModel.onCellClick(0, 2)
        viewModel.onStartingPlayerSelected(Player.O)

        viewModel.onNewGameClick()

        val state = viewModel.uiState.value
        assertFalse(state.isNewGameConfirmationShown)
        assertEquals(0, state.moveCount)
        assertEquals(Player.O, state.currentPlayer)
        assertEquals(GameStatus.IN_PROGRESS, state.status)
    }

    @Test
    fun modeChange_appliesOnlyToTheNextGame() {
        val viewModel = createViewModel()
        viewModel.onCellClick(0, 0)

        viewModel.onVsAiSelectedChange(true)

        assertFalse(viewModel.uiState.value.isVsAi)
        assertTrue(viewModel.uiState.value.isVsAiSelected)
    }
}
