package ee.taltech.tictactoecompose.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Immutable
data class GameColors(
    val boardLines: Color,
    val cellBackground: Color,
    val cellWinning: Color,
    val cellText: Color,
)

val LightGameColors = GameColors(
    boardLines = Color(0xFF424242),
    cellBackground = Color(0xFFFFFFFF),
    cellWinning = Color(0xFFA5D6A7),
    cellText = Color(0xFF212121),
)

val DarkGameColors = GameColors(
    boardLines = Color(0xFFBDBDBD),
    cellBackground = Color(0xFF1E1E1E),
    cellWinning = Color(0xFF2E7D32),
    cellText = Color(0xFFF5F5F5),
)

val LocalGameColors = staticCompositionLocalOf { LightGameColors }
