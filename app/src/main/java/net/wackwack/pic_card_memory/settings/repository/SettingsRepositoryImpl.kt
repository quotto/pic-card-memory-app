package net.wackwack.pic_card_memory.settings.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import net.wackwack.pic_card_memory.common.di.IoDispatcher
import net.wackwack.pic_card_memory.settings.model.BGMVolume
import net.wackwack.pic_card_memory.settings.model.ImagePathType
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.settings.model.Settings
import net.wackwack.pic_card_memory.settings.usecase.SettingsRepository
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineContext
):
    SettingsRepository {
    companion object {
        const val KEY_IMAGE_PATH_TYPE = "KEY_IMAGE_PATH_TYPE"
        const val KEY_IMAGE_PATH = "KEY_IMAGE_PATH"
        const val KEY_NUM_OF_CARD = "KEY_NUM_OF_CARD"
        const val KEY_BGM_VOLUME = "KEY_BGM_VOLUME"
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
        return with(preference.edit()) {
            this.putInt(KEY_IMAGE_PATH_TYPE,pathType.numValue)
            this.commit()
        }
    }

    override fun saveImagePath(path: String): Boolean {
        return with(preference.edit()) {
            this.putString(KEY_IMAGE_PATH,path)
            this.commit()
        }
    }

    override fun saveNumOfCard(numOfCard: NumOfCard): Boolean {
        return with(preference.edit()) {
            this.putInt(KEY_NUM_OF_CARD,numOfCard.numValue)
            this.commit()
        }
    }

    /**
     * SharedPreferenceにBGMの音量を保存する
     * @param volume BGMの音量
     * @return 成功した場合はtrue, 失敗した場合はfalse
     */
    override suspend fun updateBGMVolume(volume: BGMVolume): Boolean {
        return withContext(ioDispatcher) {
            with(preference.edit()) {
                this.putFloat(KEY_BGM_VOLUME, volume.volume)
                this.commit()
            }
        }
    }

    /**
     * SharedPreferenceからBGMの音量を取得する
     * 値が存在しない場合は最大値を返す
     * アプリで対応外の値が設定されている場合は最小値を返す
     * @return BGMの音量
     */
    override suspend fun getBGMVolume(): BGMVolume {
        return withContext(ioDispatcher) {
            preference.getFloat(KEY_BGM_VOLUME, BGMVolume.MAX.volume).let {
                try {
                    BGMVolume.convertToValue(it)
                } catch (e: IllegalArgumentException) {
                    Log.e(javaClass.simpleName, "Invalid BGM volume: $it, return min value")
                    BGMVolume.MIN
                }
            }
        }
    }
}