package net.wackwack.pic_card_memory.game.viewmodel

import android.content.Context
import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import net.wackwack.pic_card_memory.game.usecase.GameUseCase
import net.wackwack.pic_card_memory.game.usecase.ImageRepository
import net.wackwack.pic_card_memory.settings.model.BGMVolume
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.lang.IndexOutOfBoundsException

class GameViewModelTest {
    @Mock
    lateinit var gameUseCase: GameUseCase
    @Mock
    lateinit var imageRepository: ImageRepository
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var theme: Resources.Theme

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun before(){
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        runBlocking {
            Mockito.`when`(gameUseCase.getSavedBGM()).thenReturn(BGMVolume.MAX)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun after(){
        Mockito.clearInvocations(gameUseCase)
        Dispatchers.resetMain()
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("BGMがOFFの時はBGMをONにする")
    fun turnOnBGM() = runTest {
        Mockito.`when`(gameUseCase.getSavedBGM()).thenReturn(BGMVolume.MIN)
        // viewModelの初期化処理が終わるまで待つ
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        viewModel.toggleAudioEnabled()
        advanceUntilIdle()
        Mockito.verify(gameUseCase, Mockito.times(1)).setBGMOn()
        assertEquals(true, viewModel.audioEnabled.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("BGMがONの時はBGMをOFFにする")
    fun turnOffBGM() = runTest{
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        viewModel.toggleAudioEnabled()
        advanceUntilIdle()
        Mockito.verify(gameUseCase, Mockito.times(1)).setBGMOff()
        assertEquals(false, viewModel.audioEnabled.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("BGM再生中の場合は画面復帰処理で音楽を再開する")
    fun resumeBGMWhenAudioEnabled() = runTest{
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        // 先にaudioControlのサブスクライブを開始する
        launch {
            assertEquals(viewModel.audioControl.first(), AudioControl.Start)
        }

        viewModel.resume()
        advanceUntilIdle()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("ミュート状態の場合は画面復帰処理で音楽を再開しない")
    fun resumeBGMWhenAudioDisabled() = runTest{
        Mockito.`when`(gameUseCase.getSavedBGM()).thenReturn(BGMVolume.MIN)
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        viewModel.resume()
        advanceUntilIdle()
        try {
            viewModel.audioControl.replayCache[0]
            fail("NoSuchElementExceptionが発生しませんでした")
        } catch (e: Exception) {
            assertTrue(e is IndexOutOfBoundsException)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("viewModel初期化処理まえにresumeを呼び出した場合はBGM再開処理を行わない")
    fun resumeBGMWhenNotInitialized() = runTest{
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.resume()
        advanceUntilIdle()
        try {
            viewModel.audioControl.replayCache[0]
            fail("NoSuchElementExceptionが発生しませんでした")
        } catch (e: Exception) {
            assertTrue(e is IndexOutOfBoundsException)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("BGM再生中の場合は画面停止処理で音楽を停止する")
    fun pauseBGMWhenAudioEnabled() = runTest{
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        // 先にaudioControlのサブスクライブを開始する
        launch {
            assertEquals(viewModel.audioControl.first(), AudioControl.Pause)
        }

        viewModel.pause()
        advanceUntilIdle()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("ミュート状態の場合は画面停止処理で音楽を停止しない")
    fun pauseBGMWhenAudioDisabled() = runTest{
        Mockito.`when`(gameUseCase.getSavedBGM()).thenReturn(BGMVolume.MIN)
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.init().join()

        viewModel.pause()
        advanceUntilIdle()
        try {
            viewModel.audioControl.replayCache[0]
            fail("NoSuchElementExceptionが発生しませんでした")
        } catch (e: Exception) {
            assertTrue(e is IndexOutOfBoundsException)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @DisplayName("viewModel初期化処理まえにpauseを呼び出した場合はBGMを停止処理を行わない")
    fun pauseBGMWhenNotInitialized() = runTest{
        val viewModel = GameViewModel(gameUseCase, imageRepository, context)
        viewModel.pause()
        advanceUntilIdle()
        try {
            viewModel.audioControl.replayCache[0]
            fail("NoSuchElementExceptionが発生しませんでした")
        } catch (e: Exception) {
            assertTrue(e is IndexOutOfBoundsException)
        }
    }
}