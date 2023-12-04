package net.wackwack.pic_card_memory.game.model.computer

import net.wackwack.pic_card_memory.game.viewmodel.CardStatus
import net.wackwack.pic_card_memory.game.viewmodel.GameMessage
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel
import net.wackwack.pic_card_memory.game.viewmodel.ImageCard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * NormalComputerのテスト
 */
@Suppress("UNCHECKED_CAST")
class NormalComputerTest {
    // テスト用のViewModel
    @Mock private  lateinit var gameViewModel: GameViewModel

    @BeforeEach
    fun before(){
        MockitoAnnotations.openMocks(this)
        Mockito.`when`(gameViewModel.imageCards).thenReturn(
            mutableListOf(
                ImageCard(CardStatus.CLOSE,"1"),
                ImageCard(CardStatus.CLOSE,"2"),
                ImageCard(CardStatus.CLOSE,"3"),
                ImageCard(CardStatus.CLOSE,"4"),
                ImageCard(CardStatus.CLOSE,"5"),
                ImageCard(CardStatus.CLOSE,"6"),
                ImageCard(CardStatus.CLOSE,"1"),
                ImageCard(CardStatus.CLOSE,"2"),
                ImageCard(CardStatus.CLOSE,"3"),
                ImageCard(CardStatus.CLOSE,"4"),
                ImageCard(CardStatus.CLOSE,"5"),
                ImageCard(CardStatus.CLOSE,"6")
            )
        )
    }

    @Test
    // 自分のターンであればカードを記憶するテスト
    fun memoryCard_whenMyTurn_memory() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        // カードを記憶する
        callMemoryCard(computer, GameMessage.DoneOpen(0,"1"))

        computer.javaClass.getDeclaredField("cardMemory").apply {
            isAccessible = true
            val cardMemory: Array<String> = get(computer) as Array<String>
            // 0番目のカードが記憶されていることを確認
            assertEquals("1",cardMemory.first())
        }
    }

    @Test
    // 自分のターンでなければカードを記憶しないテスト
    fun memoryCard_whenNotMyTurn_NotMemory() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer, false)

        // カードを記憶する
        callMemoryCard(computer, GameMessage.DoneOpen(0,"1"))

        // private変数であるmemoryCardを取得する
        computer.javaClass.getDeclaredField("cardMemory").apply {
            isAccessible = true
            val cardMemory: Array<String> = get(computer) as Array<String>
            // 0番目のカードが記憶されていないことを確認
            assertEquals("",cardMemory.first())
        }
    }

    @Test
    // 1枚も記憶していない場合、ランダムに選択するテスト
    fun selectFirstCard_nothingMemory_randomSelectIndex() {
        val computer = NormalComputer(gameViewModel)
        val firstCardIndex = callSelectFirstCard(computer)
        assert(firstCardIndex in 0..11)
    }

    @Test
    //　記憶しているカードの中に重複する画像がある場合、そのカードを選択するテスト
    fun selectFirstCard_existDuplicateImageCards_selectKnownImageIndex() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        setCardMemory(computer,0,"1")
        setCardMemory(computer,9,"1")

        // インデックス0のカードが選択されることを確認
        val firstCardIndex = callSelectFirstCard(computer)
        assertEquals(0,firstCardIndex)
    }

    @Test
    // 開いたカードで記憶しているカードの中に重複する画像がある場合、そのカードは選択しないテスト
    fun selectFirstCard_existDuplicateImageCards_notSelectKnownImageIndex() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        setCardMemory(computer,0,"1")
        setCardMemory(computer,9,"1")

        setCardStatus(computer,0,true)
        setCardStatus(computer,9,true)

        // インデックス0,9のカードは選択されないことを確認
        val secondCardIndex = callSelectFirstCard(computer)
        assert(secondCardIndex != 0 && secondCardIndex != 9)
    }

    @Test
    // 記憶しているカードの中に重複する画像が複数ある場合、まだ開いていないカードを選択するテスト
    fun selectFirstCard_existMultipleDuplicateImageCards_selectClosedImageIndex() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        setCardMemory(computer,0,"1")
        setCardMemory(computer,9,"1")
        setCardMemory(computer,1,"2")
        setCardMemory(computer,10,"2")

        // cardStatusのインデックし0,9を開いた状態にする
        setCardStatus(computer,0,true)
        setCardStatus(computer,9,true)

        // インデックス1のカードが選択されることを確認
        val secondCardIndex = callSelectFirstCard(computer)
        assertEquals(1,secondCardIndex)
    }

    @Test
    // 1枚も記憶していない場合、ランダムに選択するテスト
    fun selectSecondCard_nothingMemory_randomSelectIndex() {
        val computer = NormalComputer(gameViewModel)
        setCardStatus(computer,0,true)
        setCardMemory(computer,0,"1")

        // インデックス0以外が選択されることを確認
        val secondCardIndex = callSelectSecondCard(computer, GameMessage.DoneOpen(0,"1"))
        assert(secondCardIndex != 0 && secondCardIndex >= 0)
    }

    @Test
    // 記憶しているカードの中に開いているカードと同じ画像のカードがある場合、そのカードを選択するテスト
    fun selectSecondCard_whenExistSameCardWithFirstCard_selectSameImageCardIndex() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        setCardMemory(computer,0,"1")
        setCardMemory(computer,9,"1")
        setCardMemory(computer,8,"2")

        // インデックス0のカードを開く
        setCardStatus(computer,0,true)

        // インデックス9のカードが選択されることを確認
        val secondCardIndex = callSelectSecondCard(computer, GameMessage.DoneOpen(0,"1"))
        assertEquals(9,secondCardIndex)
    }

    @Test
    // 記憶しているカードの中に開いているカードと同じ画像のカードが無い場合、記憶していないカードの中からランダムに選択するテスト
    fun selectSecondCard_whenNotExistSameCardWithFirstCard_selectRandomIndex() {
        val computer = NormalComputer(gameViewModel)

        setIsMyTurn(computer,true)

        setCardMemory(computer,0,"3")
        setCardMemory(computer,9,"1")
        setCardMemory(computer,8,"2")

        // インデックス0のカードを開く
        computer.javaClass.superclass?.getDeclaredField("cardStatus")?.apply {
            isAccessible = true
            val cardStatus: Array<Boolean> = get(computer) as Array<Boolean>
            cardStatus[0] = true
        }

        // インデックス0,8,9のカードは選択されないことを確認
        val secondCardIndex = callSelectSecondCard(computer, GameMessage.DoneOpen(0,"3"))
        assertNotEquals(0,secondCardIndex)
        assertNotEquals(8,secondCardIndex)
        assertNotEquals(9,secondCardIndex)

        // 必ずインデックスが選択されていることを確認
        assertNotEquals(-1,secondCardIndex)
    }

    // NormalComputerのisMyTurnを更新する
    private fun setIsMyTurn(computer: NormalComputer, isMyTurn: Boolean) {
        computer.javaClass.superclass?.getDeclaredField("isMyTurn")?.apply {
            isAccessible = true
            set(computer,isMyTurn)
        }
    }

    // NormalComputerのcardStatusを更新する
    private fun setCardStatus(computer: NormalComputer, index: Int, isOpen: Boolean) {
        computer.javaClass.superclass?.getDeclaredField("cardStatus")?.apply {
            isAccessible = true
            val cardStatus: Array<Boolean> = get(computer) as Array<Boolean>
            cardStatus[index] = isOpen
        }
    }

    // NormalComputerのmemoryCardListを更新する
    private fun setCardMemory(computer: NormalComputer, index: Int, imageId: String) {
        computer.javaClass.getDeclaredField("cardMemory").apply {
            isAccessible = true
            val cardMemory: Array<String> = get(computer) as Array<String>
            cardMemory[index] = imageId
        }
    }

    // memoryCardを実行する
    private fun callMemoryCard(computer: NormalComputer, message: GameMessage.DoneOpen) {
        computer.javaClass.getDeclaredMethod("memoryCard", GameMessage.DoneOpen::class.java).apply {
            isAccessible = true
            invoke(computer, message)
        }
    }

    // selectFirstCardを実行する
    private fun callSelectFirstCard(computer: NormalComputer): Int {
        return computer.javaClass.getDeclaredMethod("selectFirstCard").apply {
            isAccessible = true
        }.invoke(computer) as Int
    }

    // selectSecondCardを実行する
    private fun callSelectSecondCard(computer: NormalComputer, message: GameMessage.DoneOpen): Int {
        return computer.javaClass.getDeclaredMethod("selectSecondCard", GameMessage.DoneOpen::class.java).apply {
            isAccessible = true
        }.invoke(computer, message) as Int
    }
}