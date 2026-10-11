package ee.taltech.tictactoecompose

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.taltech.tictactoecompose.data.gameStatsDataStore
import ee.taltech.tictactoecompose.ui.game.GameViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(stringId: Int, vararg formatArguments: Any): String =
        targetContext.getString(stringId, *formatArguments)

    private fun cell(row: Int, column: Int) = composeRule.onNodeWithTag("cell_${row}_$column")

    private fun SemanticsNodeInteraction.assertSymbol(symbol: String) {
        if (symbol.isEmpty()) {
            assert(!hasText("X") and !hasText("O"))
        } else {
            assert(hasText(symbol))
        }
    }

    private fun waitForText(expected: String) {
        composeRule.waitUntil(timeoutMillis = GameViewModel.AI_MOVE_DELAY_MS + 3_000) {
            composeRule.onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Before
    fun clearStoredStatistics() {
        runBlocking { targetContext.gameStatsDataStore.edit { preferences -> preferences.clear() } }
    }

    private fun playXWinsTopRow() {
        cell(0, 0).performClick()
        cell(1, 0).performClick()
        cell(0, 1).performClick()
        cell(1, 1).performClick()
        cell(0, 2).performClick()
    }

    @Test
    fun newGame_showsXTurn() {
        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_turn, "X"))
    }

    @Test
    fun xWinsTopRow_showsWinnerText() {
        playXWinsTopRow()

        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_winner, "X"))
        cell(0, 2).assertSymbol("X")
        cell(1, 0).assertSymbol("O")
    }

    @Test
    fun tappingOccupiedCell_keepsSymbolAndTurn() {
        cell(1, 1).performClick()

        cell(1, 1).performClick()

        cell(1, 1).assertSymbol("X")
        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_turn, "O"))
    }

    @Test
    fun newGameMidGame_showsConfirmation_andCancelKeepsBoard() {
        cell(0, 0).performClick()

        composeRule.onNodeWithTag("buttonNewGame").performClick()
        composeRule.onNodeWithText(text(R.string.new_game_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("buttonCancelNewGame").performClick()

        cell(0, 0).assertSymbol("X")
    }

    @Test
    fun newGameMidGame_confirmClearsBoard() {
        cell(0, 0).performClick()

        composeRule.onNodeWithTag("buttonNewGame").performClick()
        composeRule.onNodeWithTag("buttonConfirmNewGame").performClick()

        cell(0, 0).assertSymbol("")
        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_turn, "X"))
    }

    @Test
    fun recreate_keepsBoardAndResult() {
        playXWinsTopRow()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_winner, "X"))
        cell(0, 1).assertSymbol("X")
    }

    @Test
    fun vsAiMode_aiAnswersAfterShortDelay() {
        composeRule.onNodeWithTag("switchVsAi").performClick()
        composeRule.onNodeWithTag("buttonNewGame").performClick()

        cell(1, 1).performClick()
        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_turn, "O"))

        waitForText(text(R.string.status_turn, "X"))
        cell(1, 1).assertSymbol("X")
    }

    @Test
    fun finishedGame_isCountedOnStatisticsScreen_andResetClearsIt() {
        playXWinsTopRow()

        composeRule.onNodeWithTag("buttonStatistics").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("1").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("statGamesPlayed").assertTextEquals("1")
        composeRule.onNodeWithTag("statWinsX").assertTextEquals("1")
        composeRule.onNodeWithTag("statDraws").assertTextEquals("0")

        composeRule.onNodeWithTag("buttonResetStatistics").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithText("1").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("statWinsX").assertTextEquals("0")

        composeRule.onNodeWithTag("buttonBackToGame").performClick()
        composeRule.onNodeWithTag("statusText").assertIsDisplayed()
    }
    @Test
    fun undoButton_takesBackLastMove() {
        cell(0, 0).performClick()
        cell(1, 1).performClick()

        composeRule.onNodeWithTag("buttonUndo").performClick()

        cell(1, 1).assertSymbol("")
        cell(0, 0).assertSymbol("X")
        composeRule.onNodeWithTag("statusText").assertTextEquals(text(R.string.status_turn, "O"))
    }

    @Test
    fun boardSettings_changeBoardSize() {
        composeRule.onNodeWithTag("buttonBoardSettings").performClick()
        composeRule.onNodeWithTag("stepperRowsIncrease").performClick()
        composeRule.onNodeWithTag("stepperRowsIncrease").performClick()
        composeRule.onNodeWithTag("stepperColumnsIncrease").performClick()
        composeRule.onNodeWithTag("stepperRowsValue").assertTextEquals("5")

        composeRule.onNodeWithTag("buttonApplyBoardSettings").performClick()

        cell(4, 3).assertIsDisplayed()
        composeRule.onAllNodesWithTag("cell_0_0").fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun rotateBoardButton_keepsCellsClickable() {
        composeRule.onNodeWithTag("buttonRotateBoard").performClick()
        composeRule.onNodeWithTag("buttonRotateBoard").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        cell(0, 0).performClick()

        cell(0, 0).assertSymbol("X")
    }
}
