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
    val backStack = rememberNavBackStack(GameKey)

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<GameKey> {
                    GameRoute(
                        statsRepository = statsRepository,
                        onOpenStats = { backStack.add(StatsKey) },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                entry<StatsKey> {
                    StatsRoute(
                        statsRepository = statsRepository,
                        onBackToGame = { backStack.removeLastOrNull() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            },
        )
    }
}
