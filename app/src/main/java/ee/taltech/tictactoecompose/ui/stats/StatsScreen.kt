package ee.taltech.tictactoecompose.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ee.taltech.tictactoecompose.R
import ee.taltech.tictactoecompose.Statistics
import ee.taltech.tictactoecompose.data.GameStatsRepository
import ee.taltech.tictactoecompose.ui.theme.TictactoecomposeTheme

@Composable
fun StatsRoute(
    statsRepository: GameStatsRepository,
    onBackToGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(
        factory = viewModelFactory { initializer { StatsViewModel(statsRepository) } }
    ),
) {
    // Ekraan kuulab statistikat. Andmeid ei saadeta navigeerimisel kaasa, hoidla on ainus tõde.
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    StatsScreen(
        statistics = statistics,
        onResetClick = viewModel::onResetClick,
        onBackToGame = onBackToGame,
        modifier = modifier,
    )
}

@Composable
fun StatsScreen(
    statistics: Statistics,
    onResetClick: () -> Unit,
    onBackToGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stringResource(R.string.stats_title), style = MaterialTheme.typography.headlineMedium)
        StatisticsRow(stringResource(R.string.stats_games_played), statistics.gamesPlayed.toString(), "statGamesPlayed")
        StatisticsRow(stringResource(R.string.stats_wins_x), statistics.winsX.toString(), "statWinsX")
        StatisticsRow(stringResource(R.string.stats_wins_o), statistics.winsO.toString(), "statWinsO")
        StatisticsRow(stringResource(R.string.stats_draws), statistics.draws.toString(), "statDraws")
        StatisticsRow(stringResource(R.string.stats_human_wins_vs_ai), statistics.humanWinsVsAi.toString(), "statHumanWinsVsAi")
        StatisticsRow(stringResource(R.string.stats_ai_wins_vs_human), statistics.aiWinsVsHuman.toString(), "statAiWinsVsHuman")
        // averageDurationMs on keskmine mänguaeg MILLISEKUNDITES (arvutatud Statistics.kt-s: kogukestus / mängude arv).
        // / 1_000 teeb millisekundid sekunditeks (1_000 = 1000, alakriips on ainult loetavuse jaoks).
        // Siis / 60 = täisminutid ja % 60 = üle jäänud sekundid, et näidata "2:05", mitte "125000".
        val averageSeconds = statistics.averageDurationMs / 1_000
        StatisticsRow(
            stringResource(R.string.stats_average_duration),
            stringResource(R.string.duration_format, averageSeconds / 60, averageSeconds % 60),
            "statAverageDuration",
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = androidx.compose.ui.Alignment.CenterHorizontally),
        ) {
            Button(onClick = onBackToGame, modifier = Modifier.testTag("buttonBackToGame")) {
                Text(text = stringResource(R.string.back_to_game))
            }
            Button(onClick = onResetClick, modifier = Modifier.testTag("buttonResetStatistics")) {
                Text(text = stringResource(R.string.reset_statistics))
            }
        }
    }
}

@Composable
private fun StatisticsRow(label: String, value: String, valueTag: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyLarge)
        Text(text = value, modifier = Modifier.weight(1f).testTag(valueTag), style = MaterialTheme.typography.bodyLarge)
    }
}

@PreviewLightDark
@Composable
private fun StatsScreenPreview() {
    TictactoecomposeTheme {
        StatsScreen(
            statistics = Statistics(gamesPlayed = 3, winsX = 2, draws = 1, humanWinsVsAi = 1, totalDurationMs = 90_000L),
            onResetClick = {},
            onBackToGame = {},
        )
    }
}
