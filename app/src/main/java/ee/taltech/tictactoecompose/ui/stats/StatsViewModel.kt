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

    val statistics: StateFlow<Statistics> = statsRepository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Statistics())

    fun onResetClick() {
        viewModelScope.launch {
            statsRepository.reset()
        }
    }
}
