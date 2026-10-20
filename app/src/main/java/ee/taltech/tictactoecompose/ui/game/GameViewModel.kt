package ee.taltech.tictactoecompose.ui.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.taltech.tictactoecompose.GameStatus
import ee.taltech.tictactoecompose.MinimaxAi
import ee.taltech.tictactoecompose.Player
import ee.taltech.tictactoecompose.TicTacToeGame
import ee.taltech.tictactoecompose.data.GameStatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel = mängu "aju" ekraani jaoks. Elab pööramise üle (Activity luuakse uuesti, ViewModel jääb alles),
// seega ei ole vaja onSaveInstanceState'i. Kursuse reegel: mängureeglid ei kuulu composable'isse.
class GameViewModel(
    private val statsRepository: GameStatsRepository,
    // Kell antakse lambdana ette, et testis saaks anda võltskella. elapsedRealtime = ms alates telefoni käivitamisest,
    // MITTE kellaaeg. Ei hüppa, kui kasutaja muudab kellaaega, seega sobib kestuse mõõtmiseks.
    private val currentTimeMs: () -> Long = { SystemClock.elapsedRealtime() },
    private val aiMoveDelayMs: Long = AI_MOVE_DELAY_MS,
) : ViewModel() {

    // Sama reeglite klass mis XML-äpis. ViewModel hoiab seda ja teeb sellest ekraanile hetkepildi (GameUiState).
    private var game = TicTacToeGame()
    private var aiMoveJob: Job? = null

    // Kaks muutujat: _uiState on private ja muudetav (ainult ViewModel muudab), uiState on avalik ja ainult loetav.
    // StateFlow = voog, millel on alati praegune väärtus ja mis teatab muutustest. Ekraan kuulab seda.
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onCellClick(row: Int, column: Int) {
        // Kui AI "mõtleb" (400 ms ootel), ignoreerime puudutusi, muidu saaks inimene teha AI eest käigu.
        if (isAiTurn()) return
        val moveWasMade = game.makeMove(row, column)
        if (!moveWasMade) return

        // onMoveMade kutsutakse ALATI pärast õnnestunud käiku, ka siis kui mäng just lõppes: seal salvestatakse statistika.
        onMoveMade()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    fun onVsAiSelectedChange(isVsAiSelected: Boolean) {
        // update { copy(...) }: vana seisu ei muudeta, tehakse uus koopia muudetud väljaga. ?Aatomiline? ja turvaline.
        // Lüliti asend (valik) muutub alati. Pooleli mängu režiim jääb samaks ja uus režiim kehtib alates järgmisest mängust.
        _uiState.update { state -> state.copy(isVsAiSelected = isVsAiSelected) }

        // Tühjal laual (ühtegi käiku pole) rakendub režiim kohe, sest mäng pole tegelikult veel alanud.
        if (game.moveCount == 0) {
            aiMoveJob?.cancel()
            _uiState.update { state -> state.copy(isVsAi = isVsAiSelected) }
            // Kui "O starts" on valitud ja AI lülitati sisse, on kohe AI kord ja AI teeb esimese käigu.
            if (isAiTurn()) {
                scheduleAiMove()
            }
        }
    }

    fun onStartingPlayerSelected(startingPlayer: Player) {
        _uiState.update { state -> state.copy(startingPlayerSelected = startingPlayer) }
    }

    fun onNewGameClick() {
        if (game.status == GameStatus.IN_PROGRESS && game.moveCount > 0) {
            // Dialoog on lihtsalt seisu väli. Ekraan näitab dialoogi, kui see on true. Pööramisel jääb ViewModel alles,
            // seega dialoog on pärast pööramist ikka lahti ilma lisakoodita.
            _uiState.update { state -> state.copy(isNewGameConfirmationShown = true) }
        } else {
            startNewGame()
        }
    }

    fun onNewGameConfirmed() {
        _uiState.update { state -> state.copy(isNewGameConfirmationShown = false) }
        startNewGame()
    }

    fun onNewGameDismissed() {
        _uiState.update { state -> state.copy(isNewGameConfirmationShown = false) }
    }

    fun onUndoClick() {
        // Lõppenud mängus undo ei tööta: tulemus on juba statistikas ja muidu saaks sama mängu kaks korda kirja panna.
        if (game.status != GameStatus.IN_PROGRESS || game.moveCount == 0) return

        // Kui AI ootas käiku, tühistame selle.
        aiMoveJob?.cancel()
        game.undoMove()
        // AI vastu: kui pärast tagasivõtmist on AI kord, võtame tagasi ka inimese käigu. Muidu teeks AI 400 ms pärast
        // lihtsalt uue käigu ja undo näiks nagu ei teeks midagi. Inimene tahab tagasi võtta OMA käigu.
        if (_uiState.value.isVsAi && game.currentPlayer == Player.O) {
            game.undoMove()
        }
        if (game.moveCount == 0) {
            _uiState.update { state -> state.copy(gameStartTimeMs = null) }
        }
        publishGame()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    fun onRotateBoardClick() {
        // Seisus on SIHTNURK (iga vajutus +90). Board.kt animeerib selle sihtnurgani, seega kiire topeltvajutus lõpeb 180° peal.
        _uiState.update { state -> state.copy(boardRotationDegrees = state.boardRotationDegrees + QUARTER_TURN_DEGREES) }
    }

    fun onBoardSettingsClick() {
        _uiState.update { state -> state.copy(isBoardSettingsShown = true) }
    }

    fun onBoardSettingsDismissed() {
        _uiState.update { state -> state.copy(isBoardSettingsShown = false) }
    }

    fun onBoardSettingsApplied(settings: BoardSettings) {
        // Võidurida ei saa olla pikem kui laua suurem külg (coerceAtMost = "maksimaalselt nii palju").
        val largestSide = maxOf(settings.rowCount, settings.columnCount)
        val validSettings = settings.copy(winLength = settings.winLength.coerceAtMost(largestSide))
        _uiState.update { state -> state.copy(boardSettings = validSettings, isBoardSettingsShown = false) }
        // Tühjal laual rakendub kohe, pooleli mängu ei katkesta: siis kehtib järgmisest mängust.
        if (game.moveCount == 0) {
            startNewGame()
        }
    }

    private fun startNewGame() {
        aiMoveJob?.cancel()
        val state = _uiState.value
        game = TicTacToeGame(
            rowCount = state.boardSettings.rowCount,
            columnCount = state.boardSettings.columnCount,
            winLength = state.boardSettings.winLength,
            startingPlayer = state.startingPlayerSelected,
        )
        _uiState.update { current ->
            current.copy(
                isVsAi = current.isVsAiSelected,
                gameStartTimeMs = null,
                finishedGameDurationMs = null,
            )
        }
        publishGame()
        if (isAiTurn()) {
            scheduleAiMove()
        }
    }

    private fun isAiTurn(): Boolean =
        _uiState.value.isVsAi && game.status == GameStatus.IN_PROGRESS && game.currentPlayer == Player.O

    // Korutiin viewModelScope'is: tühistatakse automaatselt, kui ViewModel hävib. delay() EI blokeeri UI lõime
    // (erinevalt Thread.sleep'ist): vahepeal joonistatakse X ekraanile. Job jäetakse meelde, et seda saaks tühistada.
    // Pööramisel ViewModel jääb alles, seega ootel käik jääb ka alles: XML-i "planeeri uuesti" trikki pole vaja.
    private fun scheduleAiMove() {
        aiMoveJob = viewModelScope.launch {
            delay(aiMoveDelayMs)
            makeAiMove()
            onMoveMade()
        }
    }

    private fun makeAiMove() {
        val bestCell = MinimaxAi.forBoard(game).chooseMove(game)
        if (bestCell != null) {
            game.makeMove(bestCell.row, bestCell.column)
        }
    }

    private fun onMoveMade() {
        val now = currentTimeMs()
        val gameStartTimeMs = _uiState.value.gameStartTimeMs ?: now
        var finishedGameDurationMs: Long? = null
        if (game.status != GameStatus.IN_PROGRESS) {
            finishedGameDurationMs = now - gameStartTimeMs
            val winner = game.winner
            val isVsAi = _uiState.value.isVsAi
            // recordGame on suspend (DataStore kirjutab kettale), seega kutsume seda korutiinis.
            viewModelScope.launch {
                statsRepository.recordGame(winner, isVsAi, finishedGameDurationMs)
            }
        }
        _uiState.update { state ->
            state.copy(gameStartTimeMs = gameStartTimeMs, finishedGameDurationMs = finishedGameDurationMs)
        }
        publishGame()
    }

    // Kopeerib mängu olulised väljad uude GameUiState'i. Kui seis muutub, joonistab Compose ekraani uuesti (recomposition).
    // game.board annab uue listi koopia, nii et vana hetkepilt ei muutu kunagi.
    private fun publishGame() {
        _uiState.update { state ->
            state.copy(
                board = game.board,
                currentPlayer = game.currentPlayer,
                status = game.status,
                winner = game.winner,
                winningCells = game.winningCells,
                moveCount = game.moveCount,
            )
        }
    }

    companion object {
        const val AI_MOVE_DELAY_MS = 400L
        private const val QUARTER_TURN_DEGREES = 90f
    }
}
