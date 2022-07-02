package net.wackwack.pic_card_memory.game.usecase

import net.wackwack.pic_card_memory.game.model.Player

class OpenResult(val players: List<Player>, val nextPlayerIndex: Int, val gameResult: GameResult)
