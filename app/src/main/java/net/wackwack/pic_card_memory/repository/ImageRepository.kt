package net.wackwack.pic_card_memory.repository

import android.graphics.Bitmap
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.wackwack.pic_card_memory.InsufficientImagesException
import net.wackwack.pic_card_memory.model.Card
import net.wackwack.pic_card_memory.model.Settings
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