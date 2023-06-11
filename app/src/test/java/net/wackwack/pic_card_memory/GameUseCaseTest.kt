package net.wackwack.pic_card_memory

import kotlinx.coroutines.runBlocking
import net.wackwack.pic_card_memory.game.model.Card
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.repository.InsufficientImagesException
import net.wackwack.pic_card_memory.game.usecase.GameRepository
import net.wackwack.pic_card_memory.game.usecase.ImageRepository
import net.wackwack.pic_card_memory.settings.usecase.SettingsRepository
import net.wackwack.pic_card_memory.game.usecase.GameUseCase
import net.wackwack.pic_card_memory.settings.model.ImagePathType
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.settings.model.Settings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class GameUseCaseTest {
    @Mock lateinit var imageRepository: ImageRepository
    @Mock lateinit var settingsRepository: SettingsRepository
    @Mock lateinit var gameRepository: GameRepository
    @InjectMocks
    lateinit var gameUseCase: GameUseCase

    @BeforeEach
    fun before(){
        MockitoAnnotations.openMocks(this)
    }

    @Nested
    @DisplayName("startGame")
    inner class StartGame {
        @Test
        @DisplayName("12枚のカードでゲームを開始する")
        fun startGame() {
            val testSettings = Settings(NumOfCard.TWELVE, ImagePathType.EXTERNAL,"")
            Mockito.doReturn(testSettings)
                .`when`(settingsRepository).getSettings()
            Mockito.doReturn(listOf(
                Card(1,"uri://1"),
                Card(2,"uri://2"),
                Card(3,"uri://3"),
                Card(4,"uri://4"),
                Card(5,"uri://5"),
                Card(6,"uri://6"),
            )).`when`(imageRepository).selectCardsBySettings(testSettings)

            val players = listOf(
                Player("p1", "#000000", PlayerType.USER),
                Player("p2", "#000000", PlayerType.USER),
            )
            runBlocking {
                gameUseCase.startGame(players).collect { cards->
                    assertEquals(12,cards.size)
                }
            }
        }

        @Test
        @DisplayName("カードが足りない場合はInsufficientImagesExceptionを投げる")
        fun startGameWhenInsufficientNumOfCard() {
            val testSettings = Settings(NumOfCard.TWELVE, ImagePathType.EXTERNAL,"")
            Mockito.doReturn(testSettings)
                .`when`(settingsRepository).getSettings()

            Mockito.doThrow(InsufficientImagesException())
                .`when`(imageRepository).selectCardsBySettings(testSettings)

            var result=false
            runBlocking {
                try {
                    gameUseCase.startGame(
                        listOf(
                            Player("p1","#000000", PlayerType.USER)
                        )
                    )
                } catch (e: InsufficientImagesException) {
                    result=true
                }
            }
            assert(result)
        }
    }
}