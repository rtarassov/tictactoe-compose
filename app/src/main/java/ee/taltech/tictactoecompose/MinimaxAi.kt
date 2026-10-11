package ee.taltech.tictactoecompose

import kotlin.random.Random

class MinimaxAi(
    private val maxDepth: Int,
    private val random: Random = Random.Default,
) {

    fun chooseMove(game: TicTacToeGame): Cell? {
        if (game.status != GameStatus.IN_PROGRESS) return null

        val aiPlayer = game.currentPlayer
        var bestScore = Int.MIN_VALUE
        var bestCell: Cell? = null
        for (cell in candidateCells(game).shuffled(random)) {
            game.makeMove(cell.row, cell.column)
            val score = minimax(game, aiPlayer, depth = 1, alpha = Int.MIN_VALUE, beta = Int.MAX_VALUE)
            game.undoMove()
            if (score > bestScore) {
                bestScore = score
                bestCell = cell
            }
        }
        return bestCell
    }

    private fun minimax(game: TicTacToeGame, aiPlayer: Player, depth: Int, alpha: Int, beta: Int): Int {
        if (game.status == GameStatus.WON) {
            return if (game.winner == aiPlayer) WIN_SCORE - depth else depth - WIN_SCORE
        }
        if (game.status == GameStatus.DRAW) {
            return DRAW_SCORE
        }
        if (depth >= maxDepth) {
            return evaluatePosition(game, aiPlayer)
        }

        val isAiTurn = game.currentPlayer == aiPlayer
        var currentAlpha = alpha
        var currentBeta = beta
        var bestScore = if (isAiTurn) Int.MIN_VALUE else Int.MAX_VALUE
        for (cell in candidateCells(game)) {
            game.makeMove(cell.row, cell.column)
            val score = minimax(game, aiPlayer, depth + 1, currentAlpha, currentBeta)
            game.undoMove()

            if (isAiTurn) {
                bestScore = maxOf(bestScore, score)
                currentAlpha = maxOf(currentAlpha, bestScore)
            } else {
                bestScore = minOf(bestScore, score)
                currentBeta = minOf(currentBeta, bestScore)
            }
            if (currentBeta <= currentAlpha) {
                break
            }
        }
        return bestScore
    }

    private fun candidateCells(game: TicTacToeGame): List<Cell> {
        val emptyCells = game.emptyCells()
        if (game.cellCount <= FULL_SEARCH_CELL_LIMIT) return emptyCells

        val cellsNextToPieces = emptyCells.filter { cell -> hasNeighbouringPiece(game, cell) }
        if (cellsNextToPieces.isNotEmpty()) return cellsNextToPieces
        return listOf(Cell(game.rowCount / 2, game.columnCount / 2))
    }

    private fun hasNeighbouringPiece(game: TicTacToeGame, cell: Cell): Boolean {
        for (rowOffset in -1..1) {
            for (columnOffset in -1..1) {
                val row = cell.row + rowOffset
                val column = cell.column + columnOffset
                if (game.isInsideBoard(row, column) && game.playerAt(row, column) != null) {
                    return true
                }
            }
        }
        return false
    }

    private fun evaluatePosition(game: TicTacToeGame, aiPlayer: Player): Int {
        var score = 0
        for (row in 0 until game.rowCount) {
            for (column in 0 until game.columnCount) {
                for (direction in WINDOW_DIRECTIONS) {
                    score += scoreWindow(game, aiPlayer, row, column, direction)
                }
            }
        }
        return score
    }

    private fun scoreWindow(game: TicTacToeGame, aiPlayer: Player, startRow: Int, startColumn: Int, direction: WindowDirection): Int {
        val endRow = startRow + direction.rowStep * (game.winLength - 1)
        val endColumn = startColumn + direction.columnStep * (game.winLength - 1)
        if (!game.isInsideBoard(endRow, endColumn)) return 0

        var aiCount = 0
        var opponentCount = 0
        for (step in 0 until game.winLength) {
            val player = game.playerAt(startRow + direction.rowStep * step, startColumn + direction.columnStep * step)
            if (player == aiPlayer) aiCount++ else if (player != null) opponentCount++
        }
        if (aiCount > 0 && opponentCount > 0) return 0
        if (aiCount > 0) return windowScore(aiCount)
        if (opponentCount > 0) return -windowScore(opponentCount)
        return 0
    }

    private fun windowScore(symbolCount: Int): Int = when (symbolCount) {
        1 -> 1
        2 -> 10
        3 -> 100
        4 -> 1_000
        else -> 10_000
    }

    private data class WindowDirection(val rowStep: Int, val columnStep: Int)

    companion object {
        private const val WIN_SCORE = 10_000_000
        private const val DRAW_SCORE = 0
        private const val FULL_SEARCH_CELL_LIMIT = 9
        private const val SMALL_BOARD_CELL_LIMIT = 16
        private const val MEDIUM_BOARD_CELL_LIMIT = 36
        private const val LARGE_BOARD_CELL_LIMIT = 64
        private const val SMALL_BOARD_DEPTH = 6
        private const val MEDIUM_BOARD_DEPTH = 4
        private const val LARGE_BOARD_DEPTH = 3
        private const val HUGE_BOARD_DEPTH = 2
        private val WINDOW_DIRECTIONS = listOf(
            WindowDirection(rowStep = 0, columnStep = 1),
            WindowDirection(rowStep = 1, columnStep = 0),
            WindowDirection(rowStep = 1, columnStep = 1),
            WindowDirection(rowStep = 1, columnStep = -1),
        )

        fun forBoard(game: TicTacToeGame, random: Random = Random.Default): MinimaxAi {
            val depth = when {
                game.cellCount <= FULL_SEARCH_CELL_LIMIT -> game.cellCount
                game.cellCount <= SMALL_BOARD_CELL_LIMIT -> SMALL_BOARD_DEPTH
                game.cellCount <= MEDIUM_BOARD_CELL_LIMIT -> MEDIUM_BOARD_DEPTH
                game.cellCount <= LARGE_BOARD_CELL_LIMIT -> LARGE_BOARD_DEPTH
                else -> HUGE_BOARD_DEPTH
            }
            return MinimaxAi(depth, random)
        }
    }
}
