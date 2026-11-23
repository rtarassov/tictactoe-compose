package ee.taltech.tictactoecompose

enum class Player {
    X, O;

    fun opponent(): Player = if (this == X) O else X
}

enum class GameStatus { IN_PROGRESS, WON, DRAW }

data class Cell(val row: Int, val column: Int)

class TicTacToeGame(
    val rowCount: Int = DEFAULT_BOARD_SIZE,
    val columnCount: Int = DEFAULT_BOARD_SIZE,
    val winLength: Int = DEFAULT_BOARD_SIZE,
    val startingPlayer: Player = Player.X,
) {

    init {
        require(rowCount in MIN_BOARD_SIZE..MAX_BOARD_SIZE) { "Row count must be between $MIN_BOARD_SIZE and $MAX_BOARD_SIZE" }
        require(columnCount in MIN_BOARD_SIZE..MAX_BOARD_SIZE) { "Column count must be between $MIN_BOARD_SIZE and $MAX_BOARD_SIZE" }
        require(winLength in MIN_WIN_LENGTH..maxOf(rowCount, columnCount)) { "Win length must fit on the board" }
    }

    private val cells: Array<Array<Player?>> = Array(rowCount) { arrayOfNulls<Player>(columnCount) }
    private val moves = mutableListOf<Cell>()

    val board: List<List<Player?>>
        get() = cells.map { row -> row.toList() }

    val moveCount: Int
        get() = moves.size

    val cellCount: Int
        get() = rowCount * columnCount

    var currentPlayer: Player = startingPlayer
        private set

    var status: GameStatus = GameStatus.IN_PROGRESS
        private set

    var winner: Player? = null
        private set

    var winningCells: List<Cell> = emptyList()
        private set

    fun playerAt(row: Int, column: Int): Player? = cells[row][column]

    fun isInsideBoard(row: Int, column: Int): Boolean = row in 0 until rowCount && column in 0 until columnCount

    fun makeMove(row: Int, column: Int): Boolean {
        if (status != GameStatus.IN_PROGRESS) return false
        if (!isInsideBoard(row, column)) return false
        if (cells[row][column] != null) return false

        cells[row][column] = currentPlayer
        // moves on undo jaoks (pööramisel hoiab mängu alles ViewModel). Võitu ei arvutata moves'ist, vaid cells massiivist.
        moves.add(Cell(row, column))
        updateStatusAfterMove(Cell(row, column))
        // Mängija vahetub alles lõpus ja ainult siis, kui mäng jätkub. Võidu korral jääb võitja "currentPlayer"-iks.
        if (status == GameStatus.IN_PROGRESS) {
            currentPlayer = currentPlayer.opponent()
        }
        return true
    }

    fun undoMove(): Boolean {
        if (moves.isEmpty()) return false

        val lastMove = moves.removeAt(moves.size - 1)
        // Käik antakse tagasi sellele, kes selle käigu tegi. loeme lahtrist, kas seal oli X või O
        currentPlayer = cells[lastMove.row][lastMove.column] ?: currentPlayer
        cells[lastMove.row][lastMove.column] = null
        status = GameStatus.IN_PROGRESS
        winner = null
        winningCells = emptyList()
        return true
    }

    fun emptyCells(): List<Cell> {
        val emptyCells = mutableListOf<Cell>()
        for (row in 0 until rowCount) {
            for (column in 0 until columnCount) {
                if (cells[row][column] == null) {
                    emptyCells.add(Cell(row, column))
                }
            }
        }
        return emptyCells
    }

    private fun updateStatusAfterMove(lastMove: Cell) {
        val winningLine = findWinningLineThrough(lastMove)
        if (winningLine != null) {
            status = GameStatus.WON
            winner = cells[lastMove.row][lastMove.column]
            winningCells = winningLine
        } else if (moves.size == cellCount) {
            status = GameStatus.DRAW
        }
    }

    // EI käi kõiki lahtreid läbi. Vaatab ainult viimase käigu lahtrit, sest uus võidurida saab tekkida ainult läbi selle.
    // Neli suunda: horisontaal, vertikaal ja kaks diagonaali. Igas suunas loeb samu sümboleid tagasi ja edasi.
    private fun findWinningLineThrough(cell: Cell): List<Cell>? {
        val player = cells[cell.row][cell.column] ?: return null
        for (direction in LINE_DIRECTIONS) {
            val backwards = collectSameSymbols(cell, player, -direction.rowStep, -direction.columnStep)
            val forwards = collectSameSymbols(cell, player, direction.rowStep, direction.columnStep)
            val line = backwards.reversed() + cell + forwards
            // VÄHEMALT winLength, mitte täpselt: ka pikem rida (nt 4 järjest, kui vaja 3) on võit.
            if (line.size >= winLength) {
                return line
            }
        }
        return null
    }

    private fun collectSameSymbols(start: Cell, player: Player, rowStep: Int, columnStep: Int): List<Cell> {
        val collected = mutableListOf<Cell>()
        var row = start.row + rowStep
        var column = start.column + columnStep
        while (isInsideBoard(row, column) && cells[row][column] == player) {
            collected.add(Cell(row, column))
            row += rowStep
            column += columnStep
        }
        return collected
    }

    private data class LineDirection(val rowStep: Int, val columnStep: Int)

    companion object {
        const val DEFAULT_BOARD_SIZE = 3
        const val MIN_BOARD_SIZE = 3
        const val MAX_BOARD_SIZE = 10
        const val MIN_WIN_LENGTH = 3
        private val LINE_DIRECTIONS = listOf(
            LineDirection(rowStep = 0, columnStep = 1),
            LineDirection(rowStep = 1, columnStep = 0),
            LineDirection(rowStep = 1, columnStep = 1),
            LineDirection(rowStep = 1, columnStep = -1),
        )
    }
}
