package net.wackwack.pic_card_memory.game.model.computer

import android.util.Log
import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

class EasyComputer(gameViewModel: GameViewModel): BasicComputer(gameViewModel) {
    override fun memoryCard(message: GameMessage.DoneOpen) {
        // 何もしない
        Log.d(javaClass.simpleName, "Never memory in this level")
    }

    override fun selectSecondCard(message: GameMessage.DoneOpen): Int {
        // まだ開いていないカードのインデックスをランダムに取得する
        return cardStatus.indices.filter { !cardStatus[it] }.random()
    }

    override fun selectFirstCard(): Int {
        // まだ開いていないカードのインデックスをランダムに取得する
        return cardStatus.indices.filter { !cardStatus[it] }.random()
    }
}