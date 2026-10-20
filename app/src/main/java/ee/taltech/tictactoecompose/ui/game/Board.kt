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
    // Laua värvid teemast (hele/tume). Mitte hardcoded värvid.
    val gameColors = LocalGameColors.current
    // Animeerib SIHTNURGANI (seisust). Kui vajutatakse uuesti poole pöörde ajal, muutub ainult siht, mitte "+90 praegusest".
    val rotationDegrees by animateFloatAsState(targetValue = state.boardRotationDegrees, label = "boardRotation")
    val largestSide = maxOf(state.rowCount, state.columnCount)
    // Mida suurem laud, seda väiksem X/O tekst (3×3 laual 40 sp).
    val cellTextSize = (40 * TicTacToeGame.DEFAULT_BOARD_SIZE / largestSide).sp
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                // aspectRatio(1f) = alati ruut, nii suur kui mahub.
                .aspectRatio(1f)
                // Pöörab ainult laua (mitte ekraani). graphicsLayer pöörab ka puudutused, seega lahtrid jäävad õigesti klikitavaks.
                .graphicsLayer { rotationZ = rotationDegrees }
                .testTag("board"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    // Mitteruudukujuline laud (nt 5×7) on ristkülik ruudu sees.
                    .aspectRatio(state.columnCount.toFloat() / state.rowCount.toFloat())
                    // Jooned = taust, mis paistab 3 dp vahedest läbi. Äärmised lahtrid on serva vastas, seega raami ei teki.
                    .background(gameColors.boardLines),
                // spacedBy = vahe AINULT elementide vahel, mitte servades.
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // Kaks tsüklit üle laua: read ja veerud. Laua suurus tuleb seisust, mitte ei ole kõvasti "9 lahtrit".
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
                                // Lahter teab oma rida ja veergu tsüklist. XML-is tuli see arvutada indeksist (/ ja %), siin pole vaja.
                                onClick = { onCellClick(row, column) },
                                // weight(1f) = kõik lahtrid reas võrdse laiusega, read Column'is võrdse kõrgusega.
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
        // Modifierite järjekord loeb: iga modifier "pakib" järgmise. background enne clickable = kogu värvitud ala on klikitav.
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
