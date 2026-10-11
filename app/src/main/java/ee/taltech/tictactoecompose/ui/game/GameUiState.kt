package ee.taltech.tictactoecompose.ui.game

import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.TicTacToeGame

data class GameUiState(
    val board: List<List<Player?>> = List(TicTacToeGame.BOARD_SIZE) { List(TicTacToeGame.BOARD_SIZE) { null } },
    val currentPlayer: Player = Player.X,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val winner: Player? = null,
    val winningCells: List<Cell> = emptyList(),
    val moveCount: Int = 0,
    val isVsAi: Boolean = false,
    val isVsAiSelected: Boolean = false,
    val startingPlayerSelected: Player = Player.X,
    val gameStartTimeMs: Long? = null,
    val finishedGameDurationMs: Long? = null,
    val isNewGameConfirmationShown: Boolean = false,
)
