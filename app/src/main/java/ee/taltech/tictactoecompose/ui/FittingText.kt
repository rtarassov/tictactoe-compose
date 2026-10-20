package ee.taltech.tictactoecompose.ui

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// Tekst, mis ei lähe kunagi üle ääre: kui ta antud ruumi ära ei mahu, teeb Compose fondi järk-järgult väiksemaks
// (kuni MIN_FONT_SIZE). Suurim suurus on stiili oma suurus. maxLines = 1: tekst ei murdu mitmele reale.
@Composable
fun FittingText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = MIN_FONT_SIZE, maxFontSize = style.fontSize),
    )
}

private val MIN_FONT_SIZE = 8.sp
