package net.wackwack.pic_card_memory.game.model

import net.wackwack.pic_card_memory.game.viewmodel.CardStatus

open class Card(val id: Long, val uriString: String, var status: CardStatus = CardStatus.CLOSE) {
    fun isPair(card: Card): Boolean {
        return this.id == card.id
    }

    fun open() {
        status = CardStatus.OPEN
    }
}
