package ee.taltech.tictactoecompose

import kotlin.random.Random

enum class Player {
    X, O;

    fun opponent(): Player = if (this == X) O else X
}

enum class GameStatus { IN_PROGRESS, WON, DRAW }

data class Cell(val row: Int, val column: Int)

class TicTacToeGame(
    startingPlayer: Player = Player.X,
    private val random: Random = Random.Default,
) {

    private val cells: Array<Array<Player?>> = Array(BOARD_SIZE) { arrayOfNulls<Player>(BOARD_SIZE) }

    val board: List<List<Player?>>
        get() = cells.map { row -> row.toList() }

    var currentPlayer: Player = startingPlayer
        private set

    var status: GameStatus = GameStatus.IN_PROGRESS
        private set

    var winner: Player? = null
        private set

    var winningCells: List<Cell> = emptyList()
        private set

    var moveCount: Int = 0
        private set

    fun makeMove(row: Int, column: Int): Boolean {
        if (status != GameStatus.IN_PROGRESS) return false
        if (row !in 0 until BOARD_SIZE || column !in 0 until BOARD_SIZE) return false
        if (cells[row][column] != null) return false

        cells[row][column] = currentPlayer
        moveCount++
        updateStatus()
        if (status == GameStatus.IN_PROGRESS) {
            currentPlayer = currentPlayer.opponent()
        }
        return true
    }

    fun makeRandomMove(): Cell? {
        if (status != GameStatus.IN_PROGRESS) return null
        val emptyCells = emptyCells()
        if (emptyCells.isEmpty()) return null
        val chosenCell = emptyCells.random(random)
        makeMove(chosenCell.row, chosenCell.column)
        return chosenCell
    }

    fun emptyCells(): List<Cell> {
        val emptyCells = mutableListOf<Cell>()
        for (row in 0 until BOARD_SIZE) {
            for (column in 0 until BOARD_SIZE) {
                if (cells[row][column] == null) {
                    emptyCells.add(Cell(row, column))
                }
            }
        }
        return emptyCells
    }

    fun toBoardString(): String {
        val boardString = StringBuilder()
        for (row in 0 until BOARD_SIZE) {
            for (column in 0 until BOARD_SIZE) {
                boardString.append(symbolOf(cells[row][column]))
            }
        }
        return boardString.toString()
    }

    private fun updateStatus() {
        val winningLine = findWinningLine()
        if (winningLine != null) {
            status = GameStatus.WON
            winner = cells[winningLine[0].row][winningLine[0].column]
            winningCells = winningLine
        } else if (moveCount == BOARD_SIZE * BOARD_SIZE) {
            status = GameStatus.DRAW
        }
    }

    private fun findWinningLine(): List<Cell>? {
        for (line in ALL_LINES) {
            val firstSymbol = cells[line[0].row][line[0].column] ?: continue
            if (line.all { cell -> cells[cell.row][cell.column] == firstSymbol }) {
                return line
            }
        }
        return null
    }

    companion object {
        const val BOARD_SIZE = 3
        private const val SYMBOL_X = 'X'
        private const val SYMBOL_O = 'O'
        private const val SYMBOL_EMPTY = '.'
        private val ALL_LINES: List<List<Cell>> = buildAllLines()

        fun fromBoardString(boardString: String, currentPlayer: Player): TicTacToeGame {
            require(boardString.length == BOARD_SIZE * BOARD_SIZE) {
                "Board string must have ${BOARD_SIZE * BOARD_SIZE} characters"
            }
            val game = TicTacToeGame(currentPlayer)
            for (index in boardString.indices) {
                val player = playerOf(boardString[index])
                if (player != null) {
                    game.cells[index / BOARD_SIZE][index % BOARD_SIZE] = player
                    game.moveCount++
                }
            }
            game.updateStatus()
            return game
        }

        private fun symbolOf(player: Player?): Char = when (player) {
            Player.X -> SYMBOL_X
            Player.O -> SYMBOL_O
            null -> SYMBOL_EMPTY
        }

        private fun playerOf(symbol: Char): Player? = when (symbol) {
            SYMBOL_X -> Player.X
            SYMBOL_O -> Player.O
            SYMBOL_EMPTY -> null
            else -> throw IllegalArgumentException("Unknown board symbol: $symbol")
        }

        private fun buildAllLines(): List<List<Cell>> {
            val lines = mutableListOf<List<Cell>>()
            for (index in 0 until BOARD_SIZE) {
                lines.add(List(BOARD_SIZE) { column -> Cell(index, column) })
            }
            for (index in 0 until BOARD_SIZE) {
                lines.add(List(BOARD_SIZE) { row -> Cell(row, index) })
            }
            lines.add(List(BOARD_SIZE) { position -> Cell(position, position) })
            lines.add(List(BOARD_SIZE) { position -> Cell(position, BOARD_SIZE - 1 - position) })
            return lines
        }
    }
}
