package net.wackwack.pic_card_memory.game.model.computer

import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

class HardComputer(gameViewModel: GameViewModel): BasicComputer(gameViewModel) {
    // 自分のめくったカードを記憶するための配列
    private var cardMemory: Array<String> = Array(gameViewModel.imageCards.size) { "" }

    override fun memoryCard(message: GameMessage.DoneOpen) {
        // 自分のターンかどうかに関わらず、めくったカードを記憶する
        cardMemory[message.target] = message.imageID
    }

    override fun selectSecondCard(message: GameMessage.DoneOpen): Int {
        // 開いていないカードの中に同じ画像のカードがあれば、そのカードのインデックスを返す
        val sameCardIndex = cardStatus.indices.filter { !cardStatus[it] }.firstOrNull { cardMemory[it] == message.imageID }
        // 記憶している同じカードが1組もなければ、必ず記憶していないカードがある
        return sameCardIndex ?: // 同じカードがなければ、開いていないカードでかつ記憶していないインデックスをランダムに取得する
                cardStatus.indices.filter { !cardStatus[it] && cardMemory[it].isEmpty() }.random() // 記憶していないカードがなければ、開いていないカードのインデックスをランダムに取得する

    }

    override fun selectFirstCard(): Int {
        // まだ開いていないカードのインデックスを取得する
        val closedCardList: List<Int> = cardStatus.indices.filter { !cardStatus[it] }

        // 開いていないカードの中に同じ画像IDが無いかチェックする
        var duplicateImageFirstIndex = -1
        for(closedIndex1 in 0 until closedCardList.size - 1) {
            for(closedIndex2 in closedIndex1+1 until closedCardList.size) {
                val image1 = cardMemory[closedCardList[closedIndex1]]
                val image2 = cardMemory[closedCardList[closedIndex2]]
                if (image1.isNotEmpty() &&
                    image1 == image2) {
                    // 同じ画像IDがあれば、そのカードのインデックスを保存する
                    duplicateImageFirstIndex =  closedCardList[closedIndex1]
                    break
                }
            }
            if (duplicateImageFirstIndex >= 0) {
                break
            }
        }

        return if(duplicateImageFirstIndex >= 0) {
            duplicateImageFirstIndex
        } else {
            // 記憶している同じカードが1組もなければ、必ず記憶していないカードがある
            // 同じ画像IDが無ければ、記憶していないかつ開いていないカードのインデックスをランダムに取得する
            cardStatus.indices.filter { !cardStatus[it] && cardMemory[it].isEmpty() }.random()
        }
    }

}