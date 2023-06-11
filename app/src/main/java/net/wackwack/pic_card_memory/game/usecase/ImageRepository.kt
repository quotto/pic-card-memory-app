package net.wackwack.pic_card_memory.game.usecase

import android.graphics.Bitmap
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.wackwack.pic_card_memory.game.repository.InsufficientImagesException
import net.wackwack.pic_card_memory.game.model.Card
import net.wackwack.pic_card_memory.settings.model.Settings
import net.wackwack.pic_card_memory.game.repository.ImageRepositoryImpl
import javax.inject.Singleton

interface ImageRepository {
    @Throws(InsufficientImagesException::class) fun selectCardsBySettings(settings: Settings): List<Card>
    fun loadImageByCard(card: Card): Bitmap
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