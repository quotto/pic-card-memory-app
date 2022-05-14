package net.wackwack.pic_card_memory.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.wackwack.pic_card_memory.model.ImagePathType
import net.wackwack.pic_card_memory.model.NumOfCard
import net.wackwack.pic_card_memory.model.Settings
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