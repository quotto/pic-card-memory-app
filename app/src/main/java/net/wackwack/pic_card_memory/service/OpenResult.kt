package net.wackwack.pic_card_memory.service

import net.wackwack.pic_card_memory.model.Player

class OpenResult(val players: List<Player>, val nextPlayerIndex: Int, val gameResult: GameResult)

enum class GameResult {
    CARD_CLOSE,
    CARD_OPEN,
    GAME_END
}