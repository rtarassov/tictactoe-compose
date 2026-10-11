package ee.taltech.tictactoecompose.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.R

@Composable
internal fun InfoPanel(
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
internal fun statusText(state: GameUiState): String = when (state.status) {
    GameStatus.IN_PROGRESS -> stringResource(R.string.status_turn, symbolText(state.currentPlayer))
    GameStatus.WON -> stringResource(R.string.status_winner, symbolText(state.winner))
    GameStatus.DRAW -> stringResource(R.string.status_draw)
}

@Composable
internal fun symbolText(player: Player?): String = when (player) {
    Player.X -> stringResource(R.string.symbol_x)
    Player.O -> stringResource(R.string.symbol_o)
    null -> ""
}
