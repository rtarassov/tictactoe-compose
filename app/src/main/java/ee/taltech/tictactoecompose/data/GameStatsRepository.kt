package ee.taltech.tictactoecompose.data

import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.Statistics
import kotlinx.coroutines.flow.Flow

// Liides: ütleb, MIDA hoidla oskab, mitte KUIDAS. ViewModel sõltub liidesest, mitte DataStore'ist,
// seega testides saab anda FakeGameStatsRepository (mälus) ja testid jooksevad ilma Androidita.
interface GameStatsRepository {
    // Flow = andmed, mis muutuvad. Ekraan saab uue väärtuse automaatselt, kui statistika muutub.
    val statistics: Flow<Statistics>

    // suspend = ühekordne tegevus, mis võib võtta aega (kettale kirjutamine). Kutsutakse korutiinist.
    suspend fun recordGame(winner: Player?, isVsAi: Boolean, durationMs: Long)

    suspend fun reset()
}
