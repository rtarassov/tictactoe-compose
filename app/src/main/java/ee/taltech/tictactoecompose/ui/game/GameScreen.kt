package ee.taltech.tictactoecompose.ui.game

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.data.GameStatsRepository
import ee.taltech.tictactoecompose.ui.theme.TictactoecomposeTheme

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
