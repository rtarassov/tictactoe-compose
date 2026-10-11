package ee.taltech.tictactoecompose.data

import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.Statistics
import kotlinx.coroutines.flow.Flow

interface GameStatsRepository {
    val statistics: Flow<Statistics>

    suspend fun recordGame(winner: Player?, isVsAi: Boolean, durationMs: Long)

    suspend fun reset()
}
