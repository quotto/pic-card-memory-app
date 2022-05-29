package net.wackwack.pic_card_memory

import net.wackwack.pic_card_memory.model.Card
import net.wackwack.pic_card_memory.model.GameBoard
import net.wackwack.pic_card_memory.model.Player
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class GameBoardTest {
    private lateinit var instance: GameBoard

    @Before
    fun before() {
        val cards = listOf(
            Card(1,"uri://1"),
            Card(2,"uri://2"),
            Card(1,"uri://1"),
            Card(2,"uri://2"),
        )
        val players = listOf(
            Player("p1","#FFFFFF"),
            Player("p2","#000000"),
        )
        instance = GameBoard(cards, players)
    }

    @Test
    fun equalityCardCompare() {
        assert(instance.compare(0,2))
    }

    @Test
    fun notEqualityCardCompare() {
        assertFalse(instance.compare(0,1))
    }

    @Test
    fun addPointToCurrentPlayer() {
        instance.addPointToCurrentPlayer()
        assertEquals(2, instance.players[instance.getCurrentPlayerIndex()].getScore())
    }

    @Test
    fun openCard() {
        instance.openCard(3)
        assertEquals(1,instance.cards[3].status)
    }

    @Test
    fun gameEnd() {
        repeat(instance.cards.size) {
            instance.openCard(it)
        }
        assert(instance.isGameEnd())
    }

    @Test
    fun notGameEnd() {
        assertFalse(instance.isGameEnd())
    }

    @Test
    fun changeTwoPlayers() {
        instance.changePlayer()
        assertEquals(1,instance.getCurrentPlayerIndex())
        instance.changePlayer()
        assertEquals(0,instance.getCurrentPlayerIndex())
    }
    @Test
    fun changeThreePlayers() {
        instance = GameBoard(listOf<Card>(),listOf(
            Player("p1","#FFFFFF"),
            Player("p2","#FFFFFF"),
            Player("p3","#FFFFFF")
        ))
        instance.changePlayer()
        assertEquals(1,instance.getCurrentPlayerIndex())
        instance.changePlayer()
        assertEquals(2,instance.getCurrentPlayerIndex())
        instance.changePlayer()
        assertEquals(0,instance.getCurrentPlayerIndex())
    }
    @Test
    fun changeOnePlayer() {
        instance = GameBoard(listOf<Card>(),listOf(
            Player("p1","#FFFFFF")
        ))
        instance.changePlayer()
        assertEquals(0,instance.getCurrentPlayerIndex())

        // 何度実行してもcurrentPlayerIndexは0
        instance.changePlayer()
        assertEquals(0,instance.getCurrentPlayerIndex())
    }

    @Test
    fun findWinnerWhenFirstPlayerIsWin() {
        instance.players[0].addScore(2)
        assertEquals("p1",instance.findWinner()?.name)
    }

    @Test
    fun findWinnerWhenSecondPlayerIsWin() {
        instance.players[1].addScore(2)
        assertEquals("p2",instance.findWinner()?.name)
    }

    @Test
    fun findWinnerWhenAllPlayersScoreIs0() {
        assertNull(instance.findWinner())
    }

    @Test
    fun findWinnerWhenAllPlayersScoreIsEven() {
        instance.players[0].addScore(2)
        instance.players[1].addScore(2)
        assertNull(instance.findWinner())
    }
}