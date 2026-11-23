package ee.taltech.tictactoecompose

import kotlin.random.Random

fun playRandomMove(game: TicTacToeGame, random: Random) {
    val cell = game.emptyCells().random(random)
    game.makeMove(cell.row, cell.column)
}

fun gameFromMoves(rowCount: Int, columnCount: Int, winLength: Int, startingPlayer: Player, moves: List<Cell>): TicTacToeGame {
    val game = TicTacToeGame(rowCount, columnCount, winLength, startingPlayer)
    for (move in moves) {
        game.makeMove(move.row, move.column)
    }
    return game
}
