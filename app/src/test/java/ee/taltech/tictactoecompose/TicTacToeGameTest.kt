package ee.taltech.tictactoecompose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TicTacToeGameTest {

    private val allLines = listOf(
        listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)),
        listOf(Cell(1, 0), Cell(1, 1), Cell(1, 2)),
        listOf(Cell(2, 0), Cell(2, 1), Cell(2, 2)),
        listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0)),
        listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1)),
        listOf(Cell(0, 2), Cell(1, 2), Cell(2, 2)),
        listOf(Cell(0, 0), Cell(1, 1), Cell(2, 2)),
        listOf(Cell(0, 2), Cell(1, 1), Cell(2, 0)),
    )

    private val allCells = (0 until 3).flatMap { row -> (0 until 3).map { column -> Cell(row, column) } }

    @Test
    fun newGame_isEmptyAndStartingPlayerMovesFirst() {
        val game = TicTacToeGame(startingPlayer = Player.O)

        assertTrue(game.board.flatten().all { cell -> cell == null })
        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertEquals(Player.O, game.currentPlayer)
        assertEquals(0, game.moveCount)
        assertNull(game.winner)
        assertTrue(game.winningCells.isEmpty())
    }

    @Test
    fun makeMove_placesSymbolAndSwitchesTurn() {
        val game = TicTacToeGame()

        val moveWasMade = game.makeMove(1, 1)

        assertTrue(moveWasMade)
        assertEquals(Player.X, game.board[1][1])
        assertEquals(Player.O, game.currentPlayer)
        assertEquals(1, game.moveCount)
    }

    @Test
    fun makeMove_onOccupiedCell_isRejected() {
        val game = TicTacToeGame()
        game.makeMove(0, 0)

        val moveWasMade = game.makeMove(0, 0)

        assertFalse(moveWasMade)
        assertEquals(Player.X, game.board[0][0])
        assertEquals(Player.O, game.currentPlayer)
        assertEquals(1, game.moveCount)
    }

    @Test
    fun makeMove_outsideBoard_isRejected() {
        val game = TicTacToeGame()

        assertFalse(game.makeMove(-1, 0))
        assertFalse(game.makeMove(0, 3))
        assertFalse(game.makeMove(3, 3))
        assertEquals(0, game.moveCount)
        assertEquals(Player.X, game.currentPlayer)
    }

    @Test
    fun everyWinningLine_isDetected() {
        for (line in allLines) {
            val game = TicTacToeGame()
            val cellsOutsideLine = allCells.filter { cell -> cell !in line }

            game.makeMove(line[0].row, line[0].column)
            game.makeMove(cellsOutsideLine[0].row, cellsOutsideLine[0].column)
            game.makeMove(line[1].row, line[1].column)
            game.makeMove(cellsOutsideLine[1].row, cellsOutsideLine[1].column)
            game.makeMove(line[2].row, line[2].column)

            assertEquals("line $line", GameStatus.WON, game.status)
            assertEquals("line $line", Player.X, game.winner)
            assertEquals("line $line", line, game.winningCells)
        }
    }

    @Test
    fun fullBoardWithoutLine_isDraw() {
        val game = TicTacToeGame()

        game.makeMove(0, 0)
        game.makeMove(0, 1)
        game.makeMove(0, 2)
        game.makeMove(1, 1)
        game.makeMove(1, 0)
        game.makeMove(1, 2)
        game.makeMove(2, 1)
        game.makeMove(2, 0)
        game.makeMove(2, 2)

        assertEquals(GameStatus.DRAW, game.status)
        assertNull(game.winner)
        assertTrue(game.winningCells.isEmpty())
        assertEquals(9, game.moveCount)
    }

    @Test
    fun lastMoveCompletingLine_isWinNotDraw() {
        val game = TicTacToeGame()

        game.makeMove(0, 0)
        game.makeMove(0, 1)
        game.makeMove(1, 1)
        game.makeMove(0, 2)
        game.makeMove(1, 0)
        game.makeMove(1, 2)
        game.makeMove(2, 1)
        game.makeMove(2, 0)
        game.makeMove(2, 2)

        assertEquals(GameStatus.WON, game.status)
        assertEquals(Player.X, game.winner)
        assertEquals(listOf(Cell(0, 0), Cell(1, 1), Cell(2, 2)), game.winningCells)
    }

    @Test
    fun makeMove_afterGameOver_isRejected() {
        val game = TicTacToeGame()
        game.makeMove(0, 0)
        game.makeMove(1, 0)
        game.makeMove(0, 1)
        game.makeMove(1, 1)
        game.makeMove(0, 2)

        val moveWasMade = game.makeMove(2, 2)

        assertFalse(moveWasMade)
        assertNull(game.board[2][2])
        assertEquals(5, game.moveCount)
        assertEquals(GameStatus.WON, game.status)
    }

    @Test
    fun toBoardString_writesRowsLeftToRightTopToBottom() {
        val game = TicTacToeGame()
        game.makeMove(0, 0)
        game.makeMove(1, 1)
        game.makeMove(2, 2)

        assertEquals("X...O...X", game.toBoardString())
    }

    @Test
    fun fromMoves_replaysHistoryAndRecomputesResult() {
        val moves = listOf(Cell(0, 0), Cell(1, 1), Cell(0, 1), Cell(1, 2), Cell(0, 2))

        val game = TicTacToeGame.fromMoves(3, 3, 3, Player.X, moves)

        assertEquals("XXX.OO...", game.toBoardString())
        assertEquals(moves, game.moveHistory)
        assertEquals(5, game.moveCount)
        assertEquals(GameStatus.WON, game.status)
        assertEquals(Player.X, game.winner)
        assertEquals(listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)), game.winningCells)
    }

    @Test
    fun fromMoves_inProgressGame_continuesWithRightPlayer() {
        val game = TicTacToeGame.fromMoves(3, 3, 3, Player.O, listOf(Cell(0, 0), Cell(1, 1)))

        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertEquals(Player.O, game.currentPlayer)
        assertEquals(Player.O, game.board[0][0])
        assertEquals(Player.X, game.board[1][1])
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromMoves_rejectsIllegalHistory() {
        TicTacToeGame.fromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0), Cell(0, 0)))
    }

    @Test
    fun undoMove_removesLastMoveAndGivesTurnBack() {
        val game = TicTacToeGame()
        game.makeMove(0, 0)
        game.makeMove(1, 1)

        val undoWasMade = game.undoMove()

        assertTrue(undoWasMade)
        assertNull(game.board[1][1])
        assertEquals(Player.O, game.currentPlayer)
        assertEquals(1, game.moveCount)
        assertEquals(listOf(Cell(0, 0)), game.moveHistory)
    }

    @Test
    fun undoMove_afterWin_reopensTheGame() {
        val game = TicTacToeGame.fromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0), Cell(1, 1), Cell(0, 1), Cell(1, 2), Cell(0, 2)))

        game.undoMove()

        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertNull(game.winner)
        assertTrue(game.winningCells.isEmpty())
        assertEquals(Player.X, game.currentPlayer)
        assertTrue(game.makeMove(2, 2))
    }

    @Test
    fun undoMove_onEmptyBoard_isRejected() {
        val game = TicTacToeGame()

        assertFalse(game.undoMove())
        assertEquals(Player.X, game.currentPlayer)
    }

    @Test
    fun customBoard_needsWinLengthInARow() {
        val game = TicTacToeGame(rowCount = 5, columnCount = 7, winLength = 4)

        game.makeMove(2, 0)
        game.makeMove(0, 0)
        game.makeMove(2, 1)
        game.makeMove(0, 1)
        game.makeMove(2, 2)
        game.makeMove(0, 2)
        assertEquals(GameStatus.IN_PROGRESS, game.status)

        game.makeMove(2, 3)

        assertEquals(GameStatus.WON, game.status)
        assertEquals(Player.X, game.winner)
        assertEquals(listOf(Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 3)), game.winningCells)
        assertEquals(5, game.rowCount)
        assertEquals(7, game.columnCount)
        assertEquals(35, game.cellCount)
    }

    @Test
    fun customBoard_detectsWinInTheMiddleOfALine() {
        val game = TicTacToeGame(rowCount = 4, columnCount = 4, winLength = 3)
        game.makeMove(0, 0)
        game.makeMove(3, 3)
        game.makeMove(2, 2)
        game.makeMove(3, 2)

        game.makeMove(1, 1)

        assertEquals(GameStatus.WON, game.status)
        assertEquals(listOf(Cell(0, 0), Cell(1, 1), Cell(2, 2)), game.winningCells)
    }

    @Test
    fun customBoard_longerRunThanWinLength_isStillAWin() {
        val game = TicTacToeGame(rowCount = 3, columnCount = 6, winLength = 3)
        game.makeMove(0, 0)
        game.makeMove(1, 0)
        game.makeMove(0, 1)
        game.makeMove(1, 1)
        game.makeMove(0, 3)
        game.makeMove(1, 3)

        game.makeMove(0, 2)

        assertEquals(GameStatus.WON, game.status)
        assertEquals(listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3)), game.winningCells)
    }

    @Test(expected = IllegalArgumentException::class)
    fun customBoard_rejectsWinLengthLongerThanBoard() {
        TicTacToeGame(rowCount = 3, columnCount = 3, winLength = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun customBoard_rejectsTooSmallBoard() {
        TicTacToeGame(rowCount = 2, columnCount = 3, winLength = 3)
    }

    @Test
    fun makeRandomMove_choosesOnlyEmptyCells() {
        val game = TicTacToeGame(random = Random(42))
        game.makeMove(0, 0)
        game.makeMove(0, 1)
        game.makeMove(0, 2)
        val emptyCellsBefore = game.emptyCells()

        val chosenCell = game.makeRandomMove()

        assertNotNull(chosenCell)
        assertTrue(chosenCell in emptyCellsBefore)
        assertEquals(Player.O, game.board[chosenCell!!.row][chosenCell.column])
        assertEquals(4, game.moveCount)
        assertEquals(Player.X, game.currentPlayer)
    }

    @Test
    fun makeRandomMove_whenGameOver_returnsNull() {
        val game = TicTacToeGame(random = Random(7))
        game.makeMove(0, 0)
        game.makeMove(1, 0)
        game.makeMove(0, 1)
        game.makeMove(1, 1)
        game.makeMove(0, 2)

        assertNull(game.makeRandomMove())
        assertEquals(5, game.moveCount)
    }

    @Test
    fun makeRandomMove_repeated_alwaysFinishesTheGame() {
        val game = TicTacToeGame(random = Random(3))

        while (game.status == GameStatus.IN_PROGRESS) {
            assertNotNull(game.makeRandomMove())
        }

        assertTrue(game.status == GameStatus.WON || game.status == GameStatus.DRAW)
        assertTrue(game.moveCount <= 9)
    }
}
