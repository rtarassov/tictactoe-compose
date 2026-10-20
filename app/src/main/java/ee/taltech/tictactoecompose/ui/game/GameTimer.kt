package ee.taltech.tictactoecompose.ui.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ee.taltech.tictactoecompose.R
import kotlinx.coroutines.delay

@Composable
internal fun GameTimer(gameStartTimeMs: Long?, finishedGameDurationMs: Long?, isInProgress: Boolean) {
    // remember = väärtus säilib recompositionite vahel (muidu oleks iga joonistusega uus). Ainult NÄIDU jaoks.
    var nowMs by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    // Korutiin, mis käivitub, kui composable ekraanile tuleb, ja tühistatakse, kui võtmed (algusaeg, kas käib) muutuvad.
    // Uuendab näitu kord sekundis. Aeg ise arvutatakse ajatemplist, mitte ei loeta sekundeid kokku.
    LaunchedEffect(gameStartTimeMs, isInProgress) {
        while (gameStartTimeMs != null && isInProgress) {
            nowMs = android.os.SystemClock.elapsedRealtime()
            delay(1_000)
        }
    }
    val elapsedMs = when {
        gameStartTimeMs == null -> 0L
        finishedGameDurationMs != null -> finishedGameDurationMs
        else -> nowMs - gameStartTimeMs
    }
    // / 60 = täisminutid, % 60 = üle jäänud sekundid (125 s → 2:05).
    val totalSeconds = elapsedMs / 1_000
    Text(
        text = stringResource(R.string.duration_format, totalSeconds / 60, totalSeconds % 60),
        style = MaterialTheme.typography.titleMedium,
    )
}
