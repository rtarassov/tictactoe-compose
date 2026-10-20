package ee.taltech.tictactoecompose.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.Statistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore asendab Compose-ajastul SharedPreferences'i: asünkroonne (ei blokeeri UI lõime) ja annab Flow.
// Top-level delegaat = üks DataStore eksemplar faili kohta (kaks eksemplari samale failile on keelatud).
val Context.gameStatsDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_stats")

class DataStoreGameStatsRepository(private val dataStore: DataStore<Preferences>) : GameStatsRepository {

    // Tüübitud võtmed: intPreferencesKey ütleb, et väärtus on Int. Vale tüüpi ei saa kogemata kirjutada.
    private object Keys {
        val GAMES_PLAYED = intPreferencesKey("games_played")
        val WINS_X = intPreferencesKey("wins_x")
        val WINS_O = intPreferencesKey("wins_o")
        val DRAWS = intPreferencesKey("draws")
        val HUMAN_WINS_VS_AI = intPreferencesKey("human_wins_vs_ai")
        val AI_WINS_VS_HUMAN = intPreferencesKey("ai_wins_vs_human")
        val TOTAL_DURATION_MS = longPreferencesKey("total_duration_ms")
    }

    override val statistics: Flow<Statistics> = dataStore.data
        // Kui faili lugemine ebaõnnestub (IOException), näitame tühja statistikat, mitte ei crashi. Muud vead visatakse edasi.
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences.toStatistics() }

    override suspend fun recordGame(winner: Player?, isVsAi: Boolean, durationMs: Long) {
        // edit { } on aatomiline: kõik väljad salvestatakse koos või mitte ükski.
        dataStore.edit { preferences ->
            val current = preferences.toStatistics()
            preferences[Keys.GAMES_PLAYED] = current.gamesPlayed + 1
            preferences[Keys.WINS_X] = current.winsX + countIf(winner == Player.X)
            preferences[Keys.WINS_O] = current.winsO + countIf(winner == Player.O)
            preferences[Keys.DRAWS] = current.draws + countIf(winner == null)
            preferences[Keys.HUMAN_WINS_VS_AI] = current.humanWinsVsAi + countIf(isVsAi && winner == Player.X)
            preferences[Keys.AI_WINS_VS_HUMAN] = current.aiWinsVsHuman + countIf(isVsAi && winner == Player.O)
            preferences[Keys.TOTAL_DURATION_MS] = current.totalDurationMs + durationMs
        }
    }

    override suspend fun reset() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    private fun Preferences.toStatistics(): Statistics = Statistics(
        // ?: 0 = kui võtit pole veel (esimene käivitus või pärast Reset'i), siis 0.
        gamesPlayed = this[Keys.GAMES_PLAYED] ?: 0,
        winsX = this[Keys.WINS_X] ?: 0,
        winsO = this[Keys.WINS_O] ?: 0,
        draws = this[Keys.DRAWS] ?: 0,
        humanWinsVsAi = this[Keys.HUMAN_WINS_VS_AI] ?: 0,
        aiWinsVsHuman = this[Keys.AI_WINS_VS_HUMAN] ?: 0,
        totalDurationMs = this[Keys.TOTAL_DURATION_MS] ?: 0L,
    )

    private fun countIf(condition: Boolean): Int = if (condition) 1 else 0
}
