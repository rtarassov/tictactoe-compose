package ee.taltech.tictactoecompose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MinimaxAiTest {

    @Test
    fun chooseMove_takesAnImmediateWin() {
        val game = gameFromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 2)))
        val ai = MinimaxAi.forBoard(game, Random(1))

        val chosenCell = ai.chooseMove(game)

        assertEquals(Cell(1, 2), chosenCell)
    }

    @Test
    fun chooseMove_blocksTheOpponentsThreat() {
        val game = gameFromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0), Cell(1, 1), Cell(0, 1)))
        val ai = MinimaxAi.forBoard(game, Random(1))

        val chosenCell = ai.chooseMove(game)

        assertEquals(Cell(0, 2), chosenCell)
    }

    @Test
    fun chooseMove_doesNotChangeTheGame() {
        val game = gameFromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0)))
        val boardBefore = game.board

        MinimaxAi.forBoard(game, Random(1)).chooseMove(game)

        assertEquals(boardBefore, game.board)
        assertEquals(1, game.moveCount)
        assertEquals(Player.O, game.currentPlayer)
    }

    @Test
    fun chooseMove_whenGameOver_returnsNull() {
        val game = gameFromMoves(3, 3, 3, Player.X, listOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(0, 2)))

        assertNull(MinimaxAi.forBoard(game, Random(1)).chooseMove(game))
    }

    @Test
    fun minimaxPlayer_neverLosesAgainstRandomOn3x3() {
        val random = Random(2026)
        repeat(40) { gameNumber ->
            val game = TicTacToeGame(startingPlayer = if (gameNumber % 2 == 0) Player.X else Player.O)
            val ai = MinimaxAi.forBoard(game, random)
            while (game.status == GameStatus.IN_PROGRESS) {
                if (game.currentPlayer == Player.O) {
                    val cell = ai.chooseMove(game)!!
                    game.makeMove(cell.row, cell.column)
                } else {
                    playRandomMove(game, random)
                }
            }
            assertNotEquals("game $gameNumber: ${game.board}", Player.X, game.winner)
        }
    }

    @Test
    fun chooseMove_onLargeBoard_findsImmediateWin() {
        val game = gameFromMoves(
            7, 7, 4, Player.X,
            listOf(Cell(0, 0), Cell(3, 3), Cell(0, 1), Cell(3, 4), Cell(0, 2), Cell(3, 5), Cell(3, 2)),
        )

        val chosenCell = MinimaxAi.forBoard(game, Random(1)).chooseMove(game)

        assertEquals(Cell(3, 6), chosenCell)
    }
    @Test
    fun chooseMove_onLargeBoard_playsNextToExistingPieces() {
        val game = gameFromMoves(7, 7, 4, Player.X, listOf(Cell(3, 3)))

        val chosenCell = MinimaxAi.forBoard(game, Random(1)).chooseMove(game)!!

        assertTrue("chosen $chosenCell", kotlin.math.abs(chosenCell.row - 3) <= 1 && kotlin.math.abs(chosenCell.column - 3) <= 1)
    }

    @Test
    fun chooseMove_onEmptyLargeBoard_startsInTheCentre() {
        val game = TicTacToeGame(7, 7, 4)

        assertEquals(Cell(3, 3), MinimaxAi.forBoard(game, Random(1)).chooseMove(game))
    }

    @Test
    fun chooseMove_onLargeBoard_blocksAnOpenThree() {
        val game = gameFromMoves(
            7, 7, 5, Player.X,
            listOf(Cell(3, 2), Cell(0, 0), Cell(3, 3), Cell(0, 1), Cell(3, 4)),
        )

        val chosenCell = MinimaxAi.forBoard(game, Random(1)).chooseMove(game)

        assertTrue("chosen $chosenCell", chosenCell == Cell(3, 1) || chosenCell == Cell(3, 5))
    }

    @Test
    fun minimaxPlayer_beatsRandomOnLargeBoardWhenHumanStarts() {
        val random = Random(11)
        var aiWins = 0
        repeat(10) {
            val game = TicTacToeGame(7, 7, 4, startingPlayer = Player.X)
            val ai = MinimaxAi.forBoard(game, random)
            while (game.status == GameStatus.IN_PROGRESS) {
                if (game.currentPlayer == Player.O) {
                    val cell = ai.chooseMove(game)!!
                    game.makeMove(cell.row, cell.column)
                } else {
                    playRandomMove(game, random)
                }
            }
            assertNotEquals(game.board.toString(), Player.X, game.winner)
            if (game.winner == Player.O) aiWins++
        }
        assertTrue("AI won only $aiWins of 10", aiWins >= 8)
    }
}
