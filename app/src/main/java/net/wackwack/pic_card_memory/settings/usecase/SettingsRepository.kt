package net.wackwack.pic_card_memory.settings.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.wackwack.pic_card_memory.settings.model.ImagePathType
import net.wackwack.pic_card_memory.settings.model.NumOfCard
import net.wackwack.pic_card_memory.settings.model.Settings
import net.wackwack.pic_card_memory.settings.repository.SettingsRepositoryImpl
import javax.inject.Singleton

interface SettingsRepository {
    fun getSettings(): Settings
    fun saveImagePathType(pathType: ImagePathType): Boolean
    fun saveImagePath(path: String): Boolean
    fun saveNumOfCard(numOfCard: NumOfCard): Boolean
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindSettingsRepositoryImpl(impl: SettingsRepositoryImpl): SettingsRepository
}