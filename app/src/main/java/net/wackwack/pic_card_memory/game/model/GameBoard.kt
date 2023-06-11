package net.wackwack.pic_card_memory.game.model

import net.wackwack.pic_card_memory.game.viewmodel.CardStatus

private const val BASIC_POINT = 2
class GameBoard(val cards: List<Card>, val players: List<Player>) {
    private var currentPlayerIndex = 0
    fun compare(card1Index: Int, card2Index: Int): Boolean {
        return cards[card1Index].isPair(cards[card2Index])
    }

    fun openCard(index: Int) {
        cards[index].open()
    }

    fun addPointToCurrentPlayer() {
        players[currentPlayerIndex].addScore(BASIC_POINT)
    }

    fun changePlayer() {
        if(currentPlayerIndex == players.size - 1) {
            currentPlayerIndex = 0
        } else {
            currentPlayerIndex++
        }
    }

    fun isGameEnd(): Boolean {
        val closedCard = cards.find { card ->
            card.status == CardStatus.CLOSE
        }

        return closedCard == null
    }

    fun getCurrentPlayerIndex(): Int {
        return currentPlayerIndex
    }

    /*
    最も高スコアのユーザーを見つけて返す
    最高スコアが同じ場合はドロー判定としてNull値を返す
     */
    fun findWinner(): Player? {
        val drawIndex = -1
        var maxScore = 0
        var winnerPlayerIndex = drawIndex
        players.forEachIndexed { index, player ->
            if(maxScore <= player.getScore()) {
                if(maxScore == player.getScore()) {
                    // 最大スコアが複数いた場合はドロー判定のためインデックスを初期値に戻す
                    winnerPlayerIndex = drawIndex
                } else {
                    winnerPlayerIndex = index
                    maxScore = player.getScore()
                }
            }
        }
        return if(winnerPlayerIndex == drawIndex) null else players[winnerPlayerIndex]
    }
}