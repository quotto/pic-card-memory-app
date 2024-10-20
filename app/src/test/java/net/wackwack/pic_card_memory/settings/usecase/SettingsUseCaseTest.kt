package net.wackwack.pic_card_memory.settings.usecase

import com.nhaarman.mockito_kotlin.capture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.wackwack.pic_card_memory.settings.model.BGMVolume
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class SettingsUseCaseTest {
    @Mock lateinit var settingsRepository: SettingsRepository
    @Captor
    lateinit var captor: ArgumentCaptor<BGMVolume>
    @InjectMocks lateinit var settingsUseCase: SettingsUseCase

    @BeforeEach
    fun before(){
        MockitoAnnotations.openMocks(this)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setVolumeToMax() = runTest {
        Mockito.`when`(settingsRepository.updateBGMVolume(BGMVolume.MAX)).thenReturn(true)
        assertTrue(settingsUseCase.setVolumeToMax())
        Mockito.verify(settingsRepository, Mockito.times(1)).updateBGMVolume(capture(captor))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setVolumeToMin() = runTest {
        Mockito.`when`(settingsRepository.updateBGMVolume(BGMVolume.MIN)).thenReturn(true)
        assertTrue(settingsUseCase.setVolumeToMin())
        Mockito.verify(settingsRepository, Mockito.times(1)).updateBGMVolume(capture(captor))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failToSetVolumeToMax() = runTest {
        Mockito.`when`(settingsRepository.updateBGMVolume(BGMVolume.MAX)).thenReturn(false)
        assertTrue(!settingsUseCase.setVolumeToMax())
        Mockito.verify(settingsRepository, Mockito.times(1)).updateBGMVolume(capture(captor))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failToSetVolumeToMin() = runTest {
        Mockito.`when`(settingsRepository.updateBGMVolume(BGMVolume.MIN)).thenReturn(false)
        assertTrue(!settingsUseCase.setVolumeToMin())
        Mockito.verify(settingsRepository, Mockito.times(1)).updateBGMVolume(capture(captor))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenMaxValue() = runTest {
        Mockito.`when`(settingsRepository.getBGMVolume()).thenReturn(BGMVolume.MAX)
        assertTrue(settingsUseCase.getBGMVolume() == BGMVolume.MAX)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenMinValue() = runTest {
        Mockito.`when`(settingsRepository.getBGMVolume()).thenReturn(BGMVolume.MIN)
        assertTrue(settingsUseCase.getBGMVolume() == BGMVolume.MIN)
    }
}