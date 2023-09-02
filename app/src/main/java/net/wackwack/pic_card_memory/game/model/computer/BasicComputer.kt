package net.wackwack.pic_card_memory.game.model.computer

import android.util.Log
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

abstract class BasicComputer(private val gameViewModel: GameViewModel): ComputerInterface {

    // カードの開閉状態を保持する配列
    protected var cardStatus:  Array<Boolean> = Array(gameViewModel.imageCards.size) {false}

    // 今自分のターンかどうかを保持する
    protected var isMyTurn = false

    // 2枚目のカードを選択したかどうかを保持する
    private var isSelectedSecondCard = false

    protected abstract fun memoryCard(message: GameMessage.DoneOpen)

    protected abstract fun selectSecondCard(message: GameMessage.DoneOpen): Int

    protected abstract fun selectFirstCard(): Int

    override fun action(message: GameMessage) {
        when(message) {
            is GameMessage.DoneOpen -> {
                Log.d(javaClass.simpleName, "Opened Card: ${message.target}")
                // カードを開く
                cardStatus[message.target] = true

                memoryCard(message)

                // 自分のターンかつまだ2枚目のカードを選択していない場合
                if(isMyTurn && !isSelectedSecondCard) {
                    // まだ開いていないカードのインデックスをランダムに取得する
                    val cardIndex = selectSecondCard(message)
                    Log.d(javaClass.simpleName, "Select Second Card: $cardIndex")
                    isSelectedSecondCard = true
                    gameViewModel.startOpenCard(cardIndex)
                }
            }
            is GameMessage.DoClose -> {
                val card1 = message.target.first
                val card2 = message.target.second

                Log.d(javaClass.simpleName, "Close: $card1, $card2")

                // カードを閉じる
                cardStatus[card1] = false
                cardStatus[card2] = false
            }
            is GameMessage.NextPlayer -> {
                isSelectedSecondCard = false
                Log.d(javaClass.simpleName, "Next: ${message.nextPlayer.name}")

                isMyTurn = message.nextPlayer.type == PlayerType.COMPUTER
                // 次のターンがコンピューターならカードのペアを選択する
                if(isMyTurn) {
                    // まだ開いていないカードのインデックスをランダムに取得する
                    val cardIndex = selectFirstCard()
                    Log.d(javaClass.simpleName, "Select First Card: $cardIndex")
                    gameViewModel.startOpenCard(cardIndex)
                }
            }

            else -> {}
        }
    }
}