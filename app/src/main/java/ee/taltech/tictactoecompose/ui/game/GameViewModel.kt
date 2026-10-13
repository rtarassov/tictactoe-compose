package ee.taltech.tictactoecompose.ui.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.MinimaxAi
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.TicTacToeGame
import ee.taltech.tictactoecompose.data.GameStatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val statsRepository: GameStatsRepository,
    private val currentTimeMs: () -> Long = { SystemClock.elapsedRealtime() },
    private val aiMoveDelayMs: Long = AI_MOVE_DELAY_MS,
) : ViewModel() {

    private var game = TicTacToeGame()
    private var aiMoveJob: Job? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onCellClick(row: Int, column: Int) {
        if (isAiTurn()) return
        val moveWasMade = game.makeMove(row, column)
        if (!moveWasMade) return

        onMoveMade()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    fun onVsAiSelectedChange(isVsAiSelected: Boolean) {
        _uiState.update { state -> state.copy(isVsAiSelected = isVsAiSelected) }
    }

    fun onStartingPlayerSelected(startingPlayer: Player) {
        _uiState.update { state -> state.copy(startingPlayerSelected = startingPlayer) }
    }

    fun onNewGameClick() {
        if (game.status == GameStatus.IN_PROGRESS && game.moveCount > 0) {
            _uiState.update { state -> state.copy(isNewGameConfirmationShown = true) }
        } else {
            startNewGame()
        }
    }

    fun onNewGameConfirmed() {
        _uiState.update { state -> state.copy(isNewGameConfirmationShown = false) }
        startNewGame()
    }

    fun onNewGameDismissed() {
        _uiState.update { state -> state.copy(isNewGameConfirmationShown = false) }
    }

    fun onUndoClick() {
        if (game.status != GameStatus.IN_PROGRESS || game.moveCount == 0) return

        aiMoveJob?.cancel()
        game.undoMove()
        if (_uiState.value.isVsAi && game.currentPlayer == Player.O) {
            game.undoMove()
        }
        if (game.moveCount == 0) {
            _uiState.update { state -> state.copy(gameStartTimeMs = null) }
        }
        publishGame()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    fun onRotateBoardClick() {
        _uiState.update { state -> state.copy(boardRotationDegrees = state.boardRotationDegrees + QUARTER_TURN_DEGREES) }
    }

    fun onBoardSettingsClick() {
        _uiState.update { state -> state.copy(isBoardSettingsShown = true) }
    }

    fun onBoardSettingsDismissed() {
        _uiState.update { state -> state.copy(isBoardSettingsShown = false) }
    }

    fun onBoardSettingsApplied(settings: BoardSettings) {
        val largestSide = maxOf(settings.rowCount, settings.columnCount)
        val validSettings = settings.copy(winLength = settings.winLength.coerceAtMost(largestSide))
        _uiState.update { state -> state.copy(boardSettings = validSettings, isBoardSettingsShown = false) }
        if (game.moveCount == 0) {
            startNewGame()
        }
    }

    private fun startNewGame() {
        aiMoveJob?.cancel()
        val state = _uiState.value
        game = TicTacToeGame(
            rowCount = state.boardSettings.rowCount,
            columnCount = state.boardSettings.columnCount,
            winLength = state.boardSettings.winLength,
            startingPlayer = state.startingPlayerSelected,
        )
        _uiState.update { current ->
            current.copy(
                isVsAi = current.isVsAiSelected,
                gameStartTimeMs = null,
                finishedGameDurationMs = null,
            )
        }
        publishGame()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    private fun isAiTurn(): Boolean =
        _uiState.value.isVsAi && game.status == GameStatus.IN_PROGRESS && game.currentPlayer == Player.O

    private fun scheduleAiMove() {
        aiMoveJob = viewModelScope.launch {
            delay(aiMoveDelayMs)
            makeAiMove()
            onMoveMade()
        }
    }

    private fun makeAiMove() {
        val bestCell = MinimaxAi.forBoard(game).chooseMove(game)
        if (bestCell != null) {
            game.makeMove(bestCell.row, bestCell.column)
        }
    }

    private fun onMoveMade() {
        val now = currentTimeMs()
        val gameStartTimeMs = _uiState.value.gameStartTimeMs ?: now
        var finishedGameDurationMs: Long? = null
        if (game.status != GameStatus.IN_PROGRESS) {
            finishedGameDurationMs = now - gameStartTimeMs
            val winner = game.winner
            val isVsAi = _uiState.value.isVsAi
            viewModelScope.launch {
                statsRepository.recordGame(winner, isVsAi, finishedGameDurationMs)
            }
        }
        _uiState.update { state ->
            state.copy(gameStartTimeMs = gameStartTimeMs, finishedGameDurationMs = finishedGameDurationMs)
        }
        publishGame()
    }

    private fun publishGame() {
        _uiState.update { state ->
            state.copy(
                board = game.board,
                currentPlayer = game.currentPlayer,
                status = game.status,
                winner = game.winner,
                winningCells = game.winningCells,
                moveCount = game.moveCount,
            )
        }
    }

    companion object {
        const val AI_MOVE_DELAY_MS = 400L
        private const val QUARTER_TURN_DEGREES = 90f
    }
}
