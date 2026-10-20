package ee.taltech.tictactoecompose.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ee.taltech.tictactoecompose.Statistics
import ee.taltech.tictactoecompose.data.GameStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatsViewModel(private val statsRepository: GameStatsRepository) : ViewModel() {

    // stateIn muudab Flow StateFlow'ks. WhileSubscribed(5_000): kuulab ainult siis, kui ekraan vaatab, ja lõpetab
    // 5 s pärast ekraanilt lahkumist (pööramine on lühem, seega pööramisel ei alustata otsast). Statistics() = algväärtus.
    val statistics: StateFlow<Statistics> = statsRepository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Statistics())

    // Pärast reset'i uueneb ekraan ise, sest statistics on Flow. Käsitsi uuesti lugeda pole vaja (XML-is oli onResume).
    fun onResetClick() {
        viewModelScope.launch {
            statsRepository.reset()
        }
    }
}
