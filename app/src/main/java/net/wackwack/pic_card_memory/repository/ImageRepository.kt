package net.wackwack.pic_card_memory.repository

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import net.wackwack.pic_card_memory.model.Card
import net.wackwack.pic_card_memory.model.Settings
import javax.inject.Singleton

interface ImageRepository {
    fun selectCardsBySettings(settings: Settings): List<Card>
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageRepositoryModule {
    @Singleton
    @Binds
    abstract  fun  bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl
    ): ImageRepository
}