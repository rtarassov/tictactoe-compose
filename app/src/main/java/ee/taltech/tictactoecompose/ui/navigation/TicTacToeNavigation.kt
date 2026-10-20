package ee.taltech.tictactoecompose.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ee.taltech.tictactoecompose.data.GameStatsRepository
import ee.taltech.tictactoecompose.ui.game.GameRoute
import ee.taltech.tictactoecompose.ui.stats.StatsRoute

@Composable
fun TicTacToeNavigation(statsRepository: GameStatsRepository, modifier: Modifier = Modifier) {
    // Back stack on tavaline list võtmetest. Ekraanil on listi VIIMANE võti. Alguses on seal ainult GameKey.
    val backStack = rememberNavBackStack(GameKey)

    // Scaffold annab innerPadding'u, et sisu ei jääks staatus- ja navigeerimisriba alla (edge-to-edge).
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            // Telefoni tagasi-nupp: eemaldab viimase võtme, ehk tagasi eelmisele ekraanile.
            onBack = { backStack.removeLastOrNull() },
            // Esimene hoiab ekraani rememberSaveable väärtused alles, teine annab igale ekraanile oma ViewModeli,
            // mis elab nii kaua kui ekraan back stackis on. Järjekord on kursuse järgi just selline.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<GameKey> {
                    GameRoute(
                        statsRepository = statsRepository,
                        // Navigeerimine = listi muutmine. Ekraan ise back stacki ei näe, saab ainult lambda.
                        onOpenStats = { backStack.add(StatsKey) },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                entry<StatsKey> {
                    StatsRoute(
                        statsRepository = statsRepository,
                        // Sama mis telefoni tagasi-nupp. Mängu ViewModel on alles, seega mäng jätkub samast kohast.
                        onBackToGame = { backStack.removeLastOrNull() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            },
        )
    }
}
