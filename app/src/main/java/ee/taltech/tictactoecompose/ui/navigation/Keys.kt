package ee.taltech.tictactoecompose.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Navigation 3 võtmed: igale ekraanile üks. @Serializable, et back stack (võtmete list) säiliks pööramisel.
@Serializable
data object GameKey : NavKey

@Serializable
data object StatsKey : NavKey
