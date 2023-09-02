package net.wackwack.pic_card_memory.settings.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import net.wackwack.pic_card_memory.settings.model.ImagePathType
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.settings.model.Settings
import net.wackwack.pic_card_memory.settings.usecase.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context):
    SettingsRepository {
    companion object {
        const val KEY_IMAGE_PATH_TYPE = "KEY_IMAGE_PATH_TYPE"
        const val KEY_IMAGE_PATH = "KEY_IMAGE_PATH"
        const val KEY_NUM_OF_CARD = "KEY_NUM_OF_CARD"
    }
    private val preference: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun getSettings(): Settings {
        val imagePathType = preference.getInt(KEY_IMAGE_PATH_TYPE, ImagePathType.EXTERNAL.numValue)
        val imagePath = preference.getString(KEY_IMAGE_PATH, "")?: ""
        val numOfCard = preference.getInt(KEY_NUM_OF_CARD, NumOfCard.TWELVE.numValue)

        return Settings(NumOfCard.convertToValue(numOfCard), ImagePathType.convertToValue(imagePathType), imagePath)
    }

    override fun saveImagePathType(pathType: ImagePathType): Boolean {
        preference.edit(commit = true) {
            this.putInt(KEY_IMAGE_PATH_TYPE,pathType.numValue)
        }
        return true
    }

    override fun saveImagePath(path: String): Boolean {
        preference.edit(commit = true) {
            this.putString(KEY_IMAGE_PATH,path)
        }
        return true
    }

    override fun saveNumOfCard(numOfCard: NumOfCard): Boolean {
        preference.edit(commit = true) {
            this.putInt(KEY_NUM_OF_CARD,numOfCard.numValue)
        }
        return true
    }
}