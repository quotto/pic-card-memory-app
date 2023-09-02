package net.wackwack.pic_card_memory.game.model

open class Player(val name: String, val color: String, val type: PlayerType) {
    init {
        if(name.isEmpty()) throw IllegalArgumentException()
        if(color.isEmpty()) throw IllegalArgumentException()
    }
    private var _score = 0


    fun addScore(point: Int) {
        _score += point
    }

    fun getScore(): Int {
        return this._score
    }
}