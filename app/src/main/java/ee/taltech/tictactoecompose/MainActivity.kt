package ee.taltech.tictactoecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ee.taltech.tictactoecompose.data.DataStoreGameStatsRepository
import ee.taltech.tictactoecompose.data.gameStatsDataStore
import ee.taltech.tictactoecompose.ui.navigation.TicTacToeNavigation
import ee.taltech.tictactoecompose.ui.theme.TictactoecomposeTheme

// Compose'is on ainult üks Activity. ComponentActivity, mitte AppCompatActivity: Compose ei vaja AppCompat'i.
// Teine ekraan (statistika) ei ole eraldi Activity, vaid Navigation 3 ekraan sama Activity sees.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Hoidla luuakse üks kord ja antakse ekraanidele edasi (Hilt on HW2-s vabatahtlik, seega teeme käsitsi).
        // applicationContext, mitte Activity: hoidla elab kauem kui üks Activity.
        val statsRepository = DataStoreGameStatsRepository(applicationContext.gameStatsDataStore)
        // setContent asendab XML-i setContentView'd: siit algab Compose UI.
        setContent {
            TictactoecomposeTheme {
                TicTacToeNavigation(statsRepository = statsRepository)
            }
        }
    }
}
