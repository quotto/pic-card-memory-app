package net.wackwack.pic_card_memory.model

import android.util.Log

class GameUseCase(numOfCard: Int){
    init {
        Log.d(javaClass.simpleName, "Start Game at $numOfCard Cards")
    }

    private var numOfClosedCard = numOfCard
    /*
    ペア判定
     */
    fun isPair(card1: Card, card2: Card): Boolean {
        if(card1.id == card2.id) {
            numOfClosedCard -= 2
            return true
        }
        return false
    }

    /*
    ゲーム終了の判定
     */
    fun isEnd(): Boolean {
        return numOfClosedCard == 0
    }

}