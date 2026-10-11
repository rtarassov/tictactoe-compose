package ee.taltech.tictactoecompose

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MinimaxAiTimingTest {

    private fun measureChooseMoveMs(rowCount: Int, columnCount: Int, winLength: Int, prefilledMoves: Int): Long {
        val game = TicTacToeGame(rowCount, columnCount, winLength, random = Random(5))
        repeat(prefilledMoves) { game.makeRandomMove() }
        val ai = MinimaxAi.forBoard(game, Random(5))
        val startedAt = System.nanoTime()
        ai.chooseMove(game)
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        println("minimax ${rowCount}x$columnCount win $winLength after $prefilledMoves moves: $elapsedMs ms")
        return elapsedMs
    }

    @Test
    fun chooseMove_staysFastOnEveryBoardSize() {
        val timings = listOf(
            measureChooseMoveMs(3, 3, 3, 0),
            measureChooseMoveMs(4, 4, 3, 0),
            measureChooseMoveMs(5, 5, 4, 0),
            measureChooseMoveMs(7, 7, 4, 0),
            measureChooseMoveMs(6, 6, 4, 12),
            measureChooseMoveMs(7, 7, 4, 10),
            measureChooseMoveMs(7, 7, 4, 25),
            measureChooseMoveMs(8, 8, 5, 30),
            measureChooseMoveMs(10, 10, 5, 0),
            measureChooseMoveMs(10, 10, 5, 20),
            measureChooseMoveMs(10, 10, 5, 50),
        )
        assertTrue("timings: $timings", timings.all { elapsedMs -> elapsedMs < 600 })
    }
}
