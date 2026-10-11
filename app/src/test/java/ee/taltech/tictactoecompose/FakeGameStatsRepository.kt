package ee.taltech.tictactoecompose

import ee.taltech.tictactoecompose.data.GameStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGameStatsRepository : GameStatsRepository {

    private val statisticsFlow = MutableStateFlow(Statistics())
    val recordedGames = mutableListOf<Triple<Player?, Boolean, Long>>()

    override val statistics: Flow<Statistics> = statisticsFlow

    override suspend fun recordGame(winner: Player?, isVsAi: Boolean, durationMs: Long) {
        recordedGames.add(Triple(winner, isVsAi, durationMs))
        val current = statisticsFlow.value
        statisticsFlow.value = current.copy(
            gamesPlayed = current.gamesPlayed + 1,
            winsX = current.winsX + if (winner == Player.X) 1 else 0,
            winsO = current.winsO + if (winner == Player.O) 1 else 0,
            draws = current.draws + if (winner == null) 1 else 0,
            totalDurationMs = current.totalDurationMs + durationMs,
        )
    }

    override suspend fun reset() {
        statisticsFlow.value = Statistics()
    }
}
