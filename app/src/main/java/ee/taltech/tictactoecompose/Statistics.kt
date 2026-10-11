package ee.taltech.tictactoecompose

data class Statistics(
    val gamesPlayed: Int = 0,
    val winsX: Int = 0,
    val winsO: Int = 0,
    val draws: Int = 0,
    val humanWinsVsAi: Int = 0,
    val aiWinsVsHuman: Int = 0,
    val totalDurationMs: Long = 0L,
) {
    val averageDurationMs: Long
        get() = if (gamesPlayed == 0) 0L else totalDurationMs / gamesPlayed
}
