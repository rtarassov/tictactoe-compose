package ee.taltech.tictactoecompose.ui.game

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
import ee.taltech.tictactoecompose.TicTacToeGame
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
        onUndoClick = viewModel::onUndoClick,
        onRotateBoardClick = viewModel::onRotateBoardClick,
        onBoardSettingsClick = viewModel::onBoardSettingsClick,
        onBoardSettingsDismissed = viewModel::onBoardSettingsDismissed,
        onBoardSettingsApplied = viewModel::onBoardSettingsApplied,
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
    onUndoClick: () -> Unit,
    onRotateBoardClick: () -> Unit,
    onBoardSettingsClick: () -> Unit,
    onBoardSettingsDismissed: () -> Unit,
    onBoardSettingsApplied: (BoardSettings) -> Unit,
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
                    onUndoClick = onUndoClick,
                    onRotateBoardClick = onRotateBoardClick,
                    onBoardSettingsClick = onBoardSettingsClick,
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
                    onUndoClick = onUndoClick,
                    onRotateBoardClick = onRotateBoardClick,
                    onBoardSettingsClick = onBoardSettingsClick,
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
    if (state.isBoardSettingsShown) {
        BoardSettingsDialog(
            currentSettings = state.boardSettings,
            onApply = onBoardSettingsApplied,
            onDismiss = onBoardSettingsDismissed,
        )
    }
}

@Composable
private fun InfoPanel(
    state: GameUiState,
    onVsAiSelectedChange: (Boolean) -> Unit,
    onStartingPlayerSelected: (Player) -> Unit,
    onNewGameClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRotateBoardClick: () -> Unit,
    onBoardSettingsClick: () -> Unit,
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
            Button(onClick = onUndoClick, enabled = state.canUndo, modifier = Modifier.testTag("buttonUndo")) {
                Text(text = stringResource(R.string.undo))
            }
            Button(onClick = onRotateBoardClick, modifier = Modifier.testTag("buttonRotateBoard")) {
                Text(text = stringResource(R.string.rotate_board))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBoardSettingsClick, modifier = Modifier.testTag("buttonBoardSettings")) {
                Text(text = stringResource(R.string.board_settings))
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
    val rotationDegrees by animateFloatAsState(targetValue = state.boardRotationDegrees, label = "boardRotation")
    val largestSide = maxOf(state.rowCount, state.columnCount)
    val cellTextSize = (40 * TicTacToeGame.DEFAULT_BOARD_SIZE / largestSide).sp
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .graphicsLayer { rotationZ = rotationDegrees }
                .testTag("board"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .aspectRatio(state.columnCount.toFloat() / state.rowCount.toFloat())
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
                                textSize = cellTextSize,
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
}

@Composable
private fun BoardCell(
    player: Player?,
    isWinningCell: Boolean,
    textSize: androidx.compose.ui.unit.TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            fontSize = textSize,
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
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("buttonConfirmNewGame")) {
                Text(text = stringResource(R.string.new_game_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("buttonCancelNewGame")) {
                Text(text = stringResource(R.string.new_game_dialog_cancel))
            }
        },
    )
}

@Composable
private fun BoardSettingsDialog(
    currentSettings: BoardSettings,
    onApply: (BoardSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var rowCount by rememberSaveable { mutableIntStateOf(currentSettings.rowCount) }
    var columnCount by rememberSaveable { mutableIntStateOf(currentSettings.columnCount) }
    var winLength by rememberSaveable { mutableIntStateOf(currentSettings.winLength) }
    var isMinimaxAi by rememberSaveable { mutableStateOf(currentSettings.isMinimaxAi) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.board_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberStepper(
                    label = stringResource(R.string.board_settings_rows),
                    value = rowCount,
                    minimum = TicTacToeGame.MIN_BOARD_SIZE,
                    maximum = TicTacToeGame.MAX_BOARD_SIZE,
                    onValueChange = { rowCount = it },
                    testTag = "stepperRows",
                )
                NumberStepper(
                    label = stringResource(R.string.board_settings_columns),
                    value = columnCount,
                    minimum = TicTacToeGame.MIN_BOARD_SIZE,
                    maximum = TicTacToeGame.MAX_BOARD_SIZE,
                    onValueChange = { columnCount = it },
                    testTag = "stepperColumns",
                )
                NumberStepper(
                    label = stringResource(R.string.board_settings_win_length),
                    value = winLength,
                    minimum = TicTacToeGame.MIN_WIN_LENGTH,
                    maximum = maxOf(rowCount, columnCount),
                    onValueChange = { winLength = it },
                    testTag = "stepperWinLength",
                )
                Text(text = stringResource(R.string.board_settings_ai_level))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isMinimaxAi, onClick = { isMinimaxAi = false })
                    Text(text = stringResource(R.string.ai_level_random))
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = isMinimaxAi, onClick = { isMinimaxAi = true }, modifier = Modifier.testTag("radioMinimax"))
                    Text(text = stringResource(R.string.ai_level_minimax))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(BoardSettings(rowCount, columnCount, winLength, isMinimaxAi)) },
                modifier = Modifier.testTag("buttonApplyBoardSettings"),
            ) {
                Text(text = stringResource(R.string.board_settings_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.board_settings_cancel)) }
        },
    )
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int,
    minimum: Int,
    maximum: Int,
    onValueChange: (Int) -> Unit,
    testTag: String,
) {
    val shownValue = value.coerceIn(minimum, maximum)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = { onValueChange(shownValue - 1) },
            enabled = shownValue > minimum,
            modifier = Modifier.testTag("${testTag}Decrease"),
        ) {
            Text(text = "−")
        }
        Text(
            text = shownValue.toString(),
            modifier = Modifier.padding(horizontal = 12.dp).testTag("${testTag}Value"),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedButton(
            onClick = { onValueChange(shownValue + 1) },
            enabled = shownValue < maximum,
            modifier = Modifier.testTag("${testTag}Increase"),
        ) {
            Text(text = "+")
        }
    }
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
            onUndoClick = {},
            onRotateBoardClick = {},
            onBoardSettingsClick = {},
            onBoardSettingsDismissed = {},
            onBoardSettingsApplied = {},
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
            onUndoClick = {},
            onRotateBoardClick = {},
            onBoardSettingsClick = {},
            onBoardSettingsDismissed = {},
            onBoardSettingsApplied = {},
            onOpenStats = {},
        )
    }
}
