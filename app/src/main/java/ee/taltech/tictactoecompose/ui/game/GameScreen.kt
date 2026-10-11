package ee.taltech.tictactoecompose.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.R
import ee.taltech.tictactoecompose.data.GameStatsRepository
import ee.taltech.tictactoecompose.ui.theme.LocalGameColors
import ee.taltech.tictactoecompose.ui.theme.TictactoecomposeTheme
import kotlinx.coroutines.delay

@Composable
fun GameRoute(
    statsRepository: GameStatsRepository,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(
        factory = viewModelFactory { initializer { GameViewModel(statsRepository) } }
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GameScreen(
        state = state,
        onCellClick = viewModel::onCellClick,
        onVsAiSelectedChange = viewModel::onVsAiSelectedChange,
        onStartingPlayerSelected = viewModel::onStartingPlayerSelected,
        onNewGameClick = viewModel::onNewGameClick,
        onNewGameConfirmed = viewModel::onNewGameConfirmed,
        onNewGameDismissed = viewModel::onNewGameDismissed,
        onOpenStats = onOpenStats,
        modifier = modifier,
    )
}

@Composable
fun GameScreen(
    state: GameUiState,
    onCellClick: (row: Int, column: Int) -> Unit,
    onVsAiSelectedChange: (Boolean) -> Unit,
    onStartingPlayerSelected: (Player) -> Unit,
    onNewGameClick: () -> Unit,
    onNewGameConfirmed: () -> Unit,
    onNewGameDismissed: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                InfoPanel(
                    state = state,
                    onVsAiSelectedChange = onVsAiSelectedChange,
                    onStartingPlayerSelected = onStartingPlayerSelected,
                    onNewGameClick = onNewGameClick,
                    onOpenStats = onOpenStats,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                Board(
                    state = state,
                    onCellClick = onCellClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                InfoPanel(
                    state = state,
                    onVsAiSelectedChange = onVsAiSelectedChange,
                    onStartingPlayerSelected = onStartingPlayerSelected,
                    onNewGameClick = onNewGameClick,
                    onOpenStats = onOpenStats,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
                Board(
                    state = state,
                    onCellClick = onCellClick,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }

    if (state.isNewGameConfirmationShown) {
        NewGameConfirmationDialog(onConfirm = onNewGameConfirmed, onDismiss = onNewGameDismissed)
    }
}

@Composable
private fun InfoPanel(
    state: GameUiState,
    onVsAiSelectedChange: (Boolean) -> Unit,
    onStartingPlayerSelected: (Player) -> Unit,
    onNewGameClick: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(R.string.mode_vs_ai))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = state.isVsAiSelected,
                onCheckedChange = onVsAiSelectedChange,
                modifier = Modifier.testTag("switchVsAi"),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            StartingPlayerOption(
                label = stringResource(R.string.starter_x),
                isSelected = state.startingPlayerSelected == Player.X,
                onSelect = { onStartingPlayerSelected(Player.X) },
            )
            Spacer(modifier = Modifier.width(8.dp))
            StartingPlayerOption(
                label = stringResource(R.string.starter_o),
                isSelected = state.startingPlayerSelected == Player.O,
                onSelect = { onStartingPlayerSelected(Player.O) },
            )
        }
        Text(
            text = statusText(state),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("statusText"),
        )
        GameTimer(
            gameStartTimeMs = state.gameStartTimeMs,
            finishedGameDurationMs = state.finishedGameDurationMs,
            isInProgress = state.status == GameStatus.IN_PROGRESS,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewGameClick, modifier = Modifier.testTag("buttonNewGame")) {
                Text(text = stringResource(R.string.new_game))
            }
            Button(onClick = onOpenStats, modifier = Modifier.testTag("buttonStatistics")) {
                Text(text = stringResource(R.string.statistics))
            }
        }
    }
}

@Composable
private fun StartingPlayerOption(label: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isSelected, onClick = onSelect, modifier = Modifier.testTag("starter$label"))
        Text(text = label)
    }
}

@Composable
private fun statusText(state: GameUiState): String = when (state.status) {
    GameStatus.IN_PROGRESS -> stringResource(R.string.status_turn, symbolText(state.currentPlayer))
    GameStatus.WON -> stringResource(R.string.status_winner, symbolText(state.winner))
    GameStatus.DRAW -> stringResource(R.string.status_draw)
}

@Composable
private fun symbolText(player: Player?): String = when (player) {
    Player.X -> stringResource(R.string.symbol_x)
    Player.O -> stringResource(R.string.symbol_o)
    null -> ""
}

@Composable
private fun GameTimer(gameStartTimeMs: Long?, finishedGameDurationMs: Long?, isInProgress: Boolean) {
    var nowMs by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    LaunchedEffect(gameStartTimeMs, isInProgress) {
        while (gameStartTimeMs != null && isInProgress) {
            nowMs = android.os.SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    val elapsedMs = when {
        gameStartTimeMs == null -> 0L
        finishedGameDurationMs != null -> finishedGameDurationMs
        else -> nowMs - gameStartTimeMs
    }
    val totalSeconds = elapsedMs / 1_000
    Text(
        text = stringResource(R.string.duration_format, totalSeconds / 60, totalSeconds % 60),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun Board(state: GameUiState, onCellClick: (row: Int, column: Int) -> Unit, modifier: Modifier = Modifier) {
    val gameColors = LocalGameColors.current
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .aspectRatio(1f)
                .background(gameColors.boardLines),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (row in state.board.indices) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    for (column in state.board[row].indices) {
                        BoardCell(
                            player = state.board[row][column],
                            isWinningCell = Cell(row, column) in state.winningCells,
                            onClick = { onCellClick(row, column) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("cell_${row}_$column"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(player: Player?, isWinningCell: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val gameColors = LocalGameColors.current
    val backgroundColor = if (isWinningCell) gameColors.cellWinning else gameColors.cellBackground
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbolText(player),
            fontSize = 40.sp,
            color = gameColors.cellText,
        )
    }
}

@Composable
private fun NewGameConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.new_game_dialog_title)) },
        text = { Text(text = stringResource(R.string.new_game_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = stringResource(R.string.new_game_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.new_game_dialog_cancel)) }
        },
    )
}

private val previewState = GameUiState(
    board = listOf(
        listOf(Player.X, Player.X, Player.X),
        listOf(null, Player.O, Player.O),
        listOf(null, null, null),
    ),
    status = GameStatus.WON,
    winner = Player.X,
    winningCells = listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)),
    moveCount = 5,
    gameStartTimeMs = 0L,
    finishedGameDurationMs = 64_000L,
)

@PreviewLightDark
@Composable
private fun GameScreenPreview() {
    TictactoecomposeTheme {
        GameScreen(
            state = previewState,
            onCellClick = { _, _ -> },
            onVsAiSelectedChange = {},
            onStartingPlayerSelected = {},
            onNewGameClick = {},
            onNewGameConfirmed = {},
            onNewGameDismissed = {},
            onOpenStats = {},
        )
    }
}

@Preview(device = "spec:width=891dp,height=411dp")
@Composable
private fun GameScreenLandscapePreview() {
    TictactoecomposeTheme {
        GameScreen(
            state = previewState,
            onCellClick = { _, _ -> },
            onVsAiSelectedChange = {},
            onStartingPlayerSelected = {},
            onNewGameClick = {},
            onNewGameConfirmed = {},
            onNewGameDismissed = {},
            onOpenStats = {},
        )
    }
}
