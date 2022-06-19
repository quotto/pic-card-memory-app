package net.wackwack.pic_card_memory.repository

import net.wackwack.pic_card_memory.model.GameBoard
import net.wackwack.pic_card_memory.model.Player
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor() : GameRepository{
    private lateinit  var memoryGameBoard: GameBoard
    private lateinit var memoryPlayers: ArrayList<Player>
    override fun registerGameBoard(gameBoard: GameBoard) {
        memoryGameBoard = gameBoard
    }

    override fun getCurrentGameBoard(): GameBoard {
        return memoryGameBoard
    }

    override fun registerPlayers(players: List<Player>) {
        memoryPlayers = ArrayList(players)
    }

    override fun updatePlayerStatus(playerIndex: Int, player: Player) {
        memoryPlayers[playerIndex] = player
    }

    override fun getPlayers(): List<Player> {
        return memoryPlayers
    }
}