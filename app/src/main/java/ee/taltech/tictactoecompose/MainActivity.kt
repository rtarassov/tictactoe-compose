package ee.taltech.tictactoecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ee.taltech.tictactoecompose.data.DataStoreGameStatsRepository
import ee.taltech.tictactoecompose.data.gameStatsDataStore
import ee.taltech.tictactoecompose.ui.navigation.TicTacToeNavigation
import ee.taltech.tictactoecompose.ui.theme.TictactoecomposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val statsRepository = DataStoreGameStatsRepository(applicationContext.gameStatsDataStore)
        setContent {
            TictactoecomposeTheme {
                TicTacToeNavigation(statsRepository = statsRepository)
            }
        }
    }
}
