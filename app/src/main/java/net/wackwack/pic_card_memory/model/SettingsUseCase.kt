package net.wackwack.pic_card_memory.model

import android.util.Log
import net.wackwack.pic_card_memory.repository.SettingsRepository
import java.lang.IllegalArgumentException
import javax.inject.Inject

class SettingsUseCase @Inject constructor (private val settingsRepository: SettingsRepository) {
    fun updateNumOfCard(numOfCard: NumOfCard): Boolean {
        // Configに書き込み
        return settingsRepository.saveNumOfCard(numOfCard)
    }

    fun updateImagePathType(pathType: ImagePathType, path: String): Boolean {
        var result = false
        if(pathType == ImagePathType.SPECIFIED && path.isEmpty()) {
            Log.e(javaClass.simpleName, "Invalid image path type: $pathType or path: $path")
        } else {
            // Configに書き込み
            settingsRepository.saveImagePathType(pathType)
            if(pathType == ImagePathType.SPECIFIED) {
                settingsRepository.saveImagePath(path)
            } else {
                // 外部ストレージまたは内部ストレージ全体の場合はフォルダ指定はクリアしておく
                settingsRepository.saveImagePath("")
            }
            result = true
        }
        return result
    }
}

