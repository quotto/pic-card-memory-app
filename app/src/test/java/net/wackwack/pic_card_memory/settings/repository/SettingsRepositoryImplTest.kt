package net.wackwack.pic_card_memory.settings.repository

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.wackwack.pic_card_memory.settings.stub.StubSharedPreferencesImpl
import net.wackwack.pic_card_memory.settings.model.BGMVolume
import net.wackwack.pic_card_memory.settings.repository.SettingsRepositoryImpl.Companion.KEY_BGM_VOLUME
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.junit.jupiter.api.Assertions.*
import org.mockito.MockitoAnnotations

class SettingsRepositoryImplTest {
    private val stubSharedPreference = StubSharedPreferencesImpl()
    @Mock lateinit var mockContext: Context
    private lateinit var settingsRepositoryImpl: SettingsRepositoryImpl

    private lateinit var mockedStaticPreferenceManager: MockedStatic<PreferenceManager>
    private lateinit var mockedStaticLog: MockedStatic<Log>

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockedStaticPreferenceManager = mockStatic(PreferenceManager::class.java)
        mockedStaticPreferenceManager.`when`<Any> { PreferenceManager.getDefaultSharedPreferences(mockContext) }
            .thenReturn(stubSharedPreference)
        mockedStaticLog = mockStatic(Log::class.java)
        settingsRepositoryImpl = SettingsRepositoryImpl(mockContext, testDispatcher)
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        stubSharedPreference.removeAll()
        mockedStaticPreferenceManager.close()
        mockedStaticLog.close()
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateBGMVolumeToMax() = runTest {
        settingsRepositoryImpl.updateBGMVolume(BGMVolume.MAX)
        assertEquals(1.0f, stubSharedPreference.getFloat(KEY_BGM_VOLUME, BGMVolume.MAX.volume))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateBGMVolumeToMin() = runTest {
        settingsRepositoryImpl.updateBGMVolume(BGMVolume.MIN)
        assertEquals(0.0f, stubSharedPreference.getFloat(KEY_BGM_VOLUME, BGMVolume.MAX.volume))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateBGMVolumeToMaxWhenExistValue() = runTest {
        stubSharedPreference.edit().putFloat(KEY_BGM_VOLUME, 0.0f).apply()
        settingsRepositoryImpl.updateBGMVolume(BGMVolume.MAX)
        assertEquals(1.0f, stubSharedPreference.getFloat(KEY_BGM_VOLUME, BGMVolume.MAX.volume))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateBGMVolumeToMinWhenExistValue() = runTest {
        stubSharedPreference.edit().putFloat(KEY_BGM_VOLUME, 1.0f).apply()
        settingsRepositoryImpl.updateBGMVolume(BGMVolume.MIN)
        assertEquals(0.0f, stubSharedPreference.getFloat(KEY_BGM_VOLUME, BGMVolume.MAX.volume))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenExistMaxValue()  = runTest{
        stubSharedPreference.edit().putFloat(KEY_BGM_VOLUME, 1.0f).apply()
        assertEquals(BGMVolume.MAX, settingsRepositoryImpl.getBGMVolume())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenNotExistValue()  = runTest  {
        assertEquals(BGMVolume.MAX, settingsRepositoryImpl.getBGMVolume())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenExistMinValue() = runTest {
        stubSharedPreference.edit().putFloat(KEY_BGM_VOLUME, 0.0f).apply()
        assertEquals(BGMVolume.MIN, settingsRepositoryImpl.getBGMVolume())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getBGMVolumeWhenExistInvalidValue() = runTest {
        stubSharedPreference.edit().putFloat(KEY_BGM_VOLUME, 0.5f).apply()
        assertEquals(BGMVolume.MIN, settingsRepositoryImpl.getBGMVolume())
    }
}