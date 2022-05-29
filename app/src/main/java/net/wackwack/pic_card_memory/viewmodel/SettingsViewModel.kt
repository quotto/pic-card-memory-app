package net.wackwack.pic_card_memory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.wackwack.pic_card_memory.service.SettingsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.wackwack.pic_card_memory.model.ImagePathType
import net.wackwack.pic_card_memory.model.NumOfCard
import net.wackwack.pic_card_memory.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val settingsUseCase: SettingsUseCase
    ): ViewModel()  {

    // 初期化時のメッセージ受信漏れを考慮してreplay=2を設定
    private val _message: MutableSharedFlow<CommandSettings> = MutableSharedFlow(replay = 2)
    val message: SharedFlow<CommandSettings> = _message

    fun init() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _message.emit(
                CommandSettings.UpdateNumOfCard(settings.numOfCard)
            )
            _message.emit(
                CommandSettings.UpdateImagePathType(
                    settings.imagePathType, settings.imagePath
                )
            )
        }
    }

    fun updateNumOfCard(numOfCard: NumOfCard) {
        viewModelScope.launch {
            if(settingsUseCase.updateNumOfCard(numOfCard)) {
                _message.emit(
                    CommandSettings.UpdateNumOfCard(numOfCard)
                )
            } else {
                _message.emit(
                    CommandSettings.Error("Invalid ImagePath")
                )
            }
        }
    }

    fun updateImagePathType(pathType: ImagePathType, path: String) {
        viewModelScope.launch {
            if(settingsUseCase.updateImagePathType(pathType, path)) {
                _message.emit(CommandSettings.UpdateImagePathType(pathType, path))
            } else {
                _message.emit(CommandSettings.Error("Invalid ImagePath"))
            }
        }
    }
}

sealed class CommandSettings {
    data class UpdateImagePathType(val pathType: ImagePathType, val path: String): CommandSettings()
    data class UpdateNumOfCard(val numOfCard: NumOfCard): CommandSettings()
    data class Error(val message: String): CommandSettings()
}