package ee.taltech.tictactoecompose.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ee.taltech.tictactoecompose.R
import ee.taltech.tictactoecompose.TicTacToeGame

@Composable
internal fun NewGameConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
internal fun BoardSettingsDialog(
    currentSettings: BoardSettings,
    onApply: (BoardSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var rowCount by rememberSaveable { mutableIntStateOf(currentSettings.rowCount) }
    var columnCount by rememberSaveable { mutableIntStateOf(currentSettings.columnCount) }
    var winLength by rememberSaveable { mutableIntStateOf(currentSettings.winLength) }

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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(BoardSettings(rowCount, columnCount, winLength)) },
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
