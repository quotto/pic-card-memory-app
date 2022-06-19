package net.wackwack.pic_card_memory

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import net.wackwack.pic_card_memory.model.*
import net.wackwack.pic_card_memory.repository.GameRepository
import net.wackwack.pic_card_memory.repository.ImageRepository
import net.wackwack.pic_card_memory.repository.SettingsRepository
import net.wackwack.pic_card_memory.service.GameUseCase
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.junit.Assert.*

class GameUseCaseTest {
    @Mock lateinit var imageRepository: ImageRepository
    @Mock lateinit var settingsRepository: SettingsRepository
    @Mock lateinit var gameRepository: GameRepository
    @InjectMocks
    lateinit var gameUseCase: GameUseCase

    @Before
    fun before(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun startGame() {
        val testSettings = Settings(NumOfCard.TWELVE,ImagePathType.EXTERNAL,"")
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
            Player("p1", "#000000"),
            Player("p2", "#000000"),
        )
        runBlocking {
            gameUseCase.startGame(players).collect { cards->
                assertEquals(12,cards.size)
            }
        }
    }

    @Test
    fun startGameWhenInsufficientNumOfCard() {
        val testSettings = Settings(NumOfCard.TWELVE,ImagePathType.EXTERNAL,"")
        Mockito.doReturn(testSettings)
            .`when`(settingsRepository).getSettings()

        Mockito.doThrow(InsufficientImagesException())
            .`when`(imageRepository).selectCardsBySettings(testSettings)

        var result=false
        runBlocking {
            try {
                gameUseCase.startGame(
                    listOf(
                        Player("p1","#000000")
                    )
                )
            } catch (e: InsufficientImagesException) {
                result=true
            }
        }
        assert(result)
    }
}