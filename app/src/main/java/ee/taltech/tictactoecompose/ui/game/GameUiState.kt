package ee.taltech.tictactoecompose.ui.game

import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.TicTacToeGame

data class BoardSettings(
    val rowCount: Int = TicTacToeGame.DEFAULT_BOARD_SIZE,
    val columnCount: Int = TicTacToeGame.DEFAULT_BOARD_SIZE,
    val winLength: Int = TicTacToeGame.DEFAULT_BOARD_SIZE,
    val isMinimaxAi: Boolean = false,
)

data class GameUiState(
    val board: List<List<Player?>> = List(TicTacToeGame.DEFAULT_BOARD_SIZE) { List(TicTacToeGame.DEFAULT_BOARD_SIZE) { null } },
    val currentPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val winner: Player? = null,
    val winningCells: List<Cell> = emptyList(),
    val moveCount: Int = 0,
    val isVsAi: Boolean = false,
    val isVsAiSelected: Boolean = false,
    val startingPlayerSelected: Player = Player.X,
    val boardSettings: BoardSettings = BoardSettings(),
    val boardRotationDegrees: Float = 0f,
    val gameStartTimeMs: Long? = null,
    val finishedGameDurationMs: Long? = null,
    val isNewGameConfirmationShown: Boolean = false,
    val isBoardSettingsShown: Boolean = false,
) {
    val rowCount: Int
        get() = board.size

    val columnCount: Int
        get() = board.firstOrNull()?.size ?: 0

    val canUndo: Boolean
        get() = status == GameStatus.IN_PROGRESS && moveCount > 0
}
