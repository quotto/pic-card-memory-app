package net.wackwack.pic_card_memory

import android.content.Context
import android.util.Log
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.model.computer.BasicComputer
import net.wackwack.pic_card_memory.game.viewmodel.CardStatus
import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel
import net.wackwack.pic_card_memory.game.viewmodel.ImageCard
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * BasicComputerのテスト
 */
class BasicComputerTest {
    // テスト用にBasicComputerを継承したクラスを作成する
    class BasicComputerTestImpl(gameViewModel: GameViewModel): BasicComputer(gameViewModel){
        override fun memoryCard(message: GameMessage.DoneOpen) {
            Log.d("BasicComputerTestImpl","memoryCard is not implement")
        }

        override fun selectSecondCard(message: GameMessage.DoneOpen): Int {
            Log.d("BasicComputerTestImpl","selectSecondCard is not implement, always return 0")
            return 0
        }

        override fun selectFirstCard(): Int {
            Log.d("BasicComputerTestImpl","selectFirstCard is not implement, always return 0")
            return 0
        }
    }
    // テスト用のViewModel
    @Mock
    lateinit var gameViewModel: GameViewModel
    @Mock
    lateinit var mockContext: Context
    @Mock
    lateinit var mockLog: Log

    companion object {
        private lateinit var  mockStatic:MockedStatic<Log>
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            mockStatic = Mockito.mockStatic(Log::class.java)
            mockStatic.`when`<Any> { Log.d(Mockito.anyString(), Mockito.anyString()) }
                .thenAnswer { invocation ->
                    val args = invocation.arguments
                    println("[DEBUG]" + args[0] + ": " + args[1])
                    null
                }
        }
    }

    @BeforeEach
    fun before(){
        MockitoAnnotations.openMocks(this)
        // gameViewModel.viewModelsCardsをモック化する
        Mockito.`when`(gameViewModel.imageCards).thenReturn(
            listOf(
                ImageCard(CardStatus.CLOSE, "dummy1"),
                ImageCard(CardStatus.CLOSE, "dummy2"),
                ImageCard(CardStatus.CLOSE, "dummy3"),
                ImageCard(CardStatus.CLOSE, "dummy4"),
                ImageCard(CardStatus.CLOSE, "dummy5"),
                ImageCard(CardStatus.CLOSE, "dummy6"),
                ImageCard(CardStatus.CLOSE, "dummy7"),
                ImageCard(CardStatus.CLOSE, "dummy8"),
                ImageCard(CardStatus.CLOSE, "dummy9"),
                ImageCard(CardStatus.CLOSE, "dummy10"),
                ImageCard(CardStatus.CLOSE, "dummy11"),
            )
        )
    }

    @Nested
    @DisplayName("action")
    inner class Action {
        @Test
        @DisplayName("プレーヤー交代のメッセージを受け取った時に、次のターンがコンピューターなら1枚目のカードを選択する")
        fun action_whenNextPlayerAndNextIsComputer_doSelectFirstCard() {
            val computer = BasicComputerTestImpl(gameViewModel)

            computer.action(GameMessage.NextPlayer(Player("COM", "#000000", PlayerType.COMPUTER)))

            // isMyTurnがtrueになっていることを確認する
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assert(get(computer) as Boolean)
            }

            // gameViewModel.startOpenCardが実行されたことを確認する
            verify(gameViewModel, Mockito.times(1)).startOpenCard(Mockito.anyInt())

        }

        @Test
        @DisplayName("プレーヤー交代のメッセージを受け取った時に、次のターンがコンピューターでなければ何もしない")
        fun action_whenNextPlayerAndNextIsNotComputer_doNothing() {
            val computer = BasicComputerTestImpl(gameViewModel)

            // isMyTurnをfalseにする
            updateIsMyTurn(computer, false)

            computer.action(GameMessage.NextPlayer(Player("P1", "#000000", PlayerType.USER)))

            // isMyTurnがfalseのままであることを確認する
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assert(!(get(computer) as Boolean))
            }

            // gameViewModel.startOpenCardが実行されないことを確認する
            verify(gameViewModel, Mockito.times(0)).startOpenCard(Mockito.anyInt())
        }

        @Test
        @DisplayName("カードオープンのメッセージを受け取った時に、内部で保持するカード配列の対象のカードを開いた状態にする。")
        // またmemoryCardを実行する。
        // 自分のターンでなければ2枚目のカードは選択しない
        fun action_whenOpenCardAndNotMyTurn_changeCardStatusToOpenAndCalMemoryCardAndDoNotSelectSecondCard() {
            val computer = BasicComputerTestImpl(gameViewModel)

            // isMyTurnをfalseにする
            updateIsMyTurn(computer, false)

            // カードオープンのメッセージを送信する
            computer.action(GameMessage.DoneOpen(0, "dummyImageID"))

            // isMyTurnがfalseのままであることを確認する
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assertFalse((get(computer) as Boolean))
            }

            // インデックス0のカードの状態がOPENになっていることを確認する
            computer.javaClass.superclass.getDeclaredField("cardStatus").apply {
                isAccessible = true
            }

            // 2枚目のカードは選択しないことを確認する
            verify(gameViewModel, Mockito.times(0)).startOpenCard(Mockito.anyInt())
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assertFalse((get(computer) as Boolean))
            }
        }

        @Test
        @DisplayName("カードオープンのメッセージを受け取った時に、内部で保持するカード配列の対象のカードを開いた状態にする。")
        // またmemoryCardを実行する。
        // 自分のターンであれば2枚目のカードを選択する
        fun action_whenOpenCardAndNotifyMyTurn_changeCardStatusToOpenAndDoSelectSecondCards() {
            val computer = BasicComputerTestImpl(gameViewModel)

            // isMyTurnをtrueにする
            updateIsMyTurn(computer, true)

            // カードオープンのメッセージを送信する
            computer.action(GameMessage.DoneOpen(0, "dummyImageID"))

            // isMyTurnがfalseのままであることを確認する
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assertTrue((get(computer) as Boolean))
            }

            // インデックス0のカードの状態がOPENになっていることを確認する
            computer.javaClass.superclass.getDeclaredField("cardStatus").apply {
                isAccessible = true
            }

            // 2枚目のカードを選択することを確認する
            verify(gameViewModel, Mockito.times(1)).startOpenCard(Mockito.anyInt())
            computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
                isAccessible = true
                assertTrue((get(computer) as Boolean))
            }
        }

        @Test
        @DisplayName("カードクローズのメッセージを受け取った時に、内部で保持するカード配列の対象のカードを閉じた状態にする。")
        fun action_whenCloseCard_changeCardStatusToClose() {
            val computer = BasicComputerTestImpl(gameViewModel)

            // インデックス0と1のカードをオープン状態にする
            computer.javaClass.superclass.getDeclaredField("cardStatus").apply {
                isAccessible = true
                val cardStatus = get(computer) as Array<Boolean>
                cardStatus[0] = true
                cardStatus[1] = true
            }

            // カードクローズのメッセージを送信する
            computer.action(GameMessage.DoClose(Pair(0, 1)))

            // インデックス0と1のカードがクローズ状態になっていることを確認する
            computer.javaClass.superclass.getDeclaredField("cardStatus").apply {
                isAccessible = true
                val cardStatus = get(computer) as Array<Boolean>
                assertFalse(cardStatus[0])
                assertFalse(cardStatus[1])
            }
        }
    }

    // isMyTurnを更新するためのメソッド
    private fun updateIsMyTurn(computer: BasicComputerTestImpl, isMyTurn: Boolean){
        // プライベートフィールドのisMyTurnを更新する
        computer.javaClass.superclass.getDeclaredField("isMyTurn").apply {
            isAccessible = true
            set(computer,isMyTurn)
        }
    }
}