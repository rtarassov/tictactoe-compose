package ee.taltech.tictactoecompose.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.taltech.tictactoecompose.Cell
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.TicTacToeGame
import ee.taltech.tictactoecompose.ui.theme.LocalGameColors

@Composable
internal fun Board(state: GameUiState, onCellClick: (row: Int, column: Int) -> Unit, modifier: Modifier = Modifier) {
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
