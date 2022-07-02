package net.wackwack.pic_card_memory.game.model.computer

import net.wackwack.pic_card_memory.game.viewmodel.GameMessage

/**
 * コンピューターの振る舞いを定義するインターフェース
 */
interface ComputerInterface {
    // メッセージをパラメータとしてアクションを取る
    fun action(message: GameMessage)
}