package net.wackwack.pic_card_memory.model

open class Player(val name: String, val color: String) {
    init {
        if(name.isEmpty()) throw IllegalArgumentException()
        if(color.isEmpty()) throw IllegalArgumentException()
    }
    private var score = 0

    fun addScore(point: Int) {
        score += point
    }

    fun getScore(): Int {
        return this.score
    }
}