package net.wackwack.pic_card_memory.model

open class Card(val id: Long, val uriString: String, var status: Int = 0) {
    fun isPair(card: Card): Boolean {
        return this.id == card.id
    }

    fun open() {
        status = 1
    }
}
