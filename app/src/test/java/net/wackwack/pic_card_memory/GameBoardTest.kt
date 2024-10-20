package net.wackwack.pic_card_memory

import net.wackwack.pic_card_memory.game.model.Card
import net.wackwack.pic_card_memory.game.model.GameBoard
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.viewmodel.CardStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GameBoardTest {
    private lateinit var instance: GameBoard

    @BeforeEach
    fun before() {
        val cards = listOf(
            Card(1,"uri://1"),
            Card(2,"uri://2"),
            Card(1,"uri://1"),
            Card(2,"uri://2"),
        )
        val players = listOf(
            Player("p1","#FFFFFF", PlayerType.USER),
            Player("p2","#000000", PlayerType.USER),
        )
        instance = GameBoard(cards, players)
    }

    @Nested
    @DisplayName("compare")
    inner class Compare {
        @Test
        @DisplayName("カードの数が2であればtrueを返す")
        fun equalityCardCompare() {
            assert(instance.compare(0,2))
        }

        @Test
        @DisplayName("カードの数が2でなければfalseを返す")
        fun notEqualityCardCompare() {
            assertFalse(instance.compare(0,1))
        }
    }

    @Nested
    @DisplayName("addPointToCurrentPlayer")
    inner class AddPointToCurrentPlayer {
        @Test
        @DisplayName("現在のプレイヤーのポイントを1増やす")
        fun addPointToCurrentPlayer() {
            instance.addPointToCurrentPlayer()
            assertEquals(2, instance.players[instance.getCurrentPlayerIndex()].getScore())
        }
    }

    @Nested
    @DisplayName("openCard")
    inner class OpenCard {
        @Test
        @DisplayName("カードを開く")
        fun openCard() {
            instance.openCard(3)
            assertEquals(CardStatus.OPEN,instance.cards[3].status)
        }
    }

    @Nested
    @DisplayName("isGameEnd")
    inner class GameEnd {
        @Test
        @DisplayName("ゲームが終了していればtrueを返す")
        fun gameEnd() {
            repeat(instance.cards.size) {
                instance.openCard(it)
            }
            assert(instance.isGameEnd())
        }

        @Test
        @DisplayName("ゲームが終了していなければfalseを返す")
        fun notGameEnd() {
            assertFalse(instance.isGameEnd())
        }
    }

    @Nested
    @DisplayName("changePlayer")
    inner class ChangePlayer {
        @Test
        @DisplayName("プレーヤーが2人の時は交互に交代すること")
        fun changeTwoPlayers() {
            instance.changePlayer()
            assertEquals(1,instance.getCurrentPlayerIndex())
            instance.changePlayer()
            assertEquals(0,instance.getCurrentPlayerIndex())
        }
        @Test
        @DisplayName("プレーヤーが3人の時は順番に交代すること")
        fun changeThreePlayers() {
            instance = GameBoard(listOf(),listOf(
                Player("p1","#FFFFFF", PlayerType.USER),
                Player("p2","#FFFFFF", PlayerType.USER),
                Player("p3","#FFFFFF", PlayerType.USER)
            ))
            instance.changePlayer()
            assertEquals(1,instance.getCurrentPlayerIndex())
            instance.changePlayer()
            assertEquals(2,instance.getCurrentPlayerIndex())
            instance.changePlayer()
            assertEquals(0,instance.getCurrentPlayerIndex())
        }
        @Test
        @DisplayName("プレーヤーが1人の時はcurrentPlayerが変わらないこと")
        fun changeOnePlayer() {
            instance = GameBoard(listOf(),listOf(
                Player("p1","#FFFFFF", PlayerType.USER)
            ))
            instance.changePlayer()
            assertEquals(0,instance.getCurrentPlayerIndex())

            // 何度実行してもcurrentPlayerIndexは0
            instance.changePlayer()
            assertEquals(0,instance.getCurrentPlayerIndex())
        }
    }

    @Nested
    @DisplayName("findWinner")
    inner class FindWinner {
        @Test
        @DisplayName("プレイヤー1が勝利した場合はプレイヤー1を返す")
        fun findWinnerWhenOnePlayerWin() {
            instance.players[0].addScore(2)
            assertEquals("p1",instance.findWinner()?.name)
        }
        @Test
        @DisplayName("プレイヤー2が勝利した場合はプレイヤー2を返す")
        fun findWinnerWhenSecondPlayerIsWin() {
            instance.players[1].addScore(2)
            assertEquals("p2",instance.findWinner()?.name)
        }

        @Test
        @DisplayName("全員のスコアが0の場合はnullを返す")
        fun findWinnerWhenAllPlayersScoreIs0() {
            assertNull(instance.findWinner())
        }

        @Test
        @DisplayName("全員のスコアが同じ場合はnullを返す")
        fun findWinnerWhenAllPlayersScoreIsEven() {
            instance.players[0].addScore(2)
            instance.players[1].addScore(2)
            assertNull(instance.findWinner())
        }
    }

}