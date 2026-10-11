package ee.taltech.tictactoecompose

import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsTest {

    @Test
    fun averageDuration_isTotalDividedByGames() {
        val statistics = Statistics(gamesPlayed = 2, winsX = 1, draws = 1, totalDurationMs = 8000L)

        assertEquals(4000L, statistics.averageDurationMs)
    }

    @Test
    fun averageDuration_withNoGames_isZero() {
        val statistics = Statistics()

        assertEquals(0L, statistics.averageDurationMs)
    }
}
