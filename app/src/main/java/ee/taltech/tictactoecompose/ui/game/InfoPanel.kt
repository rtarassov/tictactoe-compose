package ee.taltech.tictactoecompose.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.R
import ee.taltech.tictactoecompose.ui.FittingText

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
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // weight(1f, fill = false): tekst saab ainult selle ruumi, mis lülitist üle jääb, ja ei võta rohkem kui vaja.
            FittingText(
                text = stringResource(R.string.mode_vs_ai),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Value/event paar: lüliti näitab seisu (checked) ja teatab muutusest (onCheckedChange). Ise ta seisu ei hoia.
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
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            StartingPlayerOption(
                label = stringResource(R.string.starter_o),
                isSelected = state.startingPlayerSelected == Player.O,
                onSelect = { onStartingPlayerSelected(Player.O) },
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        FittingText(
            text = statusText(state),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("statusText"),
        )
        GameTimer(
            gameStartTimeMs = state.gameStartTimeMs,
            finishedGameDurationMs = state.finishedGameDurationMs,
            isInProgress = state.status == GameStatus.IN_PROGRESS,
        )
        // fillMaxWidth + iga nupp weight(1f): rida on täpselt paneeli laiune ja nupud jagavad selle võrdselt,
        // seega rida ei saa kunagi ekraanist laiemaks minna.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameButton(
                text = stringResource(R.string.new_game),
                onClick = onNewGameClick,
                modifier = Modifier.weight(1f).testTag("buttonNewGame"),
            )
            // enabled = state.canUndo: nupp on hall, kui mäng on läbi või ühtegi käiku pole.
            GameButton(
                text = stringResource(R.string.undo),
                onClick = onUndoClick,
                enabled = state.canUndo,
                modifier = Modifier.weight(1f).testTag("buttonUndo"),
            )
            GameButton(
                text = stringResource(R.string.rotate_board),
                onClick = onRotateBoardClick,
                modifier = Modifier.weight(1f).testTag("buttonRotateBoard"),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameButton(
                text = stringResource(R.string.board_settings),
                onClick = onBoardSettingsClick,
                modifier = Modifier.weight(1f).testTag("buttonBoardSettings"),
            )
            GameButton(
                text = stringResource(R.string.statistics),
                onClick = onOpenStats,
                modifier = Modifier.weight(1f).testTag("buttonStatistics"),
            )
        }
    }
}

// Üks nupp, mille tekst mahub alati nupu sisse. Väiksem sisemine vahe (contentPadding), et tekstile jääks rohkem ruumi.
@Composable
private fun GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        FittingText(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StartingPlayerOption(label: String, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = isSelected, onClick = onSelect, modifier = Modifier.testTag("starter$label"))
        FittingText(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun statusText(state: GameUiState): String = when (state.status) {
    // stringResource + %s: üks tekst "%s wins!" kõigile võitjatele, tekstid strings.xml-is (tõlgitavad).
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
