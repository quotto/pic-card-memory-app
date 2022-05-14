package net.wackwack.pic_card_memory

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import net.wackwack.pic_card_memory.model.ImagePathType
import net.wackwack.pic_card_memory.model.NumOfCard
import net.wackwack.pic_card_memory.repository.SettingsRepositoryImpl
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class SettingsRepositoryImplTest {
    private val settingsRepositoryImpl = SettingsRepositoryImpl(ApplicationProvider.getApplicationContext())
    private val sharedPreference = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        sharedPreference.edit {
            remove(SettingsRepositoryImpl.KEY_NUM_OF_CARD)
            remove(SettingsRepositoryImpl.KEY_IMAGE_PATH)
            remove(SettingsRepositoryImpl.KEY_IMAGE_PATH_TYPE)
            commit()
        }
    }

    @Test
    fun getSettingsWhenNothingSavedSettings() {
        val settings = settingsRepositoryImpl.getSettings()
        assertEquals(NumOfCard.TWELVE, settings.numOfCard)
        assertEquals(ImagePathType.EXTERNAL, settings.imagePathType)
        assertEquals("", settings.imagePath)
    }

    @Test
    fun saveNumOfCard() {
        settingsRepositoryImpl.saveNumOfCard(NumOfCard.THIRTY)
        val settings = settingsRepositoryImpl.getSettings()
        assertEquals(NumOfCard.THIRTY,settings.numOfCard)
    }

    @Test
    fun saveImagePathType() {
        settingsRepositoryImpl.saveImagePathType(ImagePathType.SPECIFIED)
        val settings = settingsRepositoryImpl.getSettings()
        assertEquals(ImagePathType.SPECIFIED,settings.imagePathType)
    }

    @Test
    fun saveImagePath() {
        settingsRepositoryImpl.saveImagePath("content://test")
        val settings = settingsRepositoryImpl.getSettings()
        assertEquals("content://test",settings.imagePath )
    }
}