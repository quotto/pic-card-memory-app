package net.wackwack.pic_card_memory

import android.content.Context
import net.wackwack.pic_card_memory.model.Card
import net.wackwack.pic_card_memory.model.ImagePathType
import net.wackwack.pic_card_memory.model.NumOfCard
import net.wackwack.pic_card_memory.model.Settings
import net.wackwack.pic_card_memory.repository.ImageRepositoryImpl
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(ImageRepositoryImpl::class)
class ImageRepositoryImplTest {

    private val context: Context = Mockito.mock(Context::class.java)
    private val spyImageRepositoryImpl = PowerMockito.spy(ImageRepositoryImpl(context))

    @Test
    fun loadImageWhenImageTypeIsExternal() {
        val requiredNumOfCard = NumOfCard.TWELVE.numValue.div(2)
        val mockResult = List(requiredNumOfCard) { Card(1L, "content://test", 0) }
        PowerMockito.doReturn(mockResult).`when`(spyImageRepositoryImpl,"loadFromExternalStorage", requiredNumOfCard)
        spyImageRepositoryImpl.selectCardsBySettings(Settings(NumOfCard.TWELVE,ImagePathType.EXTERNAL,""))

        PowerMockito.verifyPrivate(spyImageRepositoryImpl, times(1)).invoke("loadFromExternalStorage",requiredNumOfCard)
    }

    @Test
    fun loadImageWhenImageTypeIsSpecified() {
        val requiredNumOfCard = NumOfCard.THIRTY.numValue.div(2)
        val mockResult = List(requiredNumOfCard) { Card(1L, "content://test", 0) }
        PowerMockito.doReturn(mockResult).`when`(spyImageRepositoryImpl,"loadFromDocumentTree", "content://test",requiredNumOfCard)
        spyImageRepositoryImpl.selectCardsBySettings(Settings(NumOfCard.THIRTY,ImagePathType.SPECIFIED,"content://test"))

        PowerMockito.verifyPrivate(spyImageRepositoryImpl, times(1)).invoke("loadFromDocumentTree","content://test",requiredNumOfCard)
    }

    @Test
    fun occurInsufficientImagesException() {
        val requiredNumOfCard = NumOfCard.THIRTY.numValue.div(2)
        val mockResult = List(requiredNumOfCard-1) { Card(1L, "content://test", 0) }
        PowerMockito.doReturn(mockResult).`when`(spyImageRepositoryImpl,"loadFromExternalStorage", requiredNumOfCard)
        try {
            spyImageRepositoryImpl.selectCardsBySettings(Settings(NumOfCard.THIRTY,
                ImagePathType.EXTERNAL,
                ""))
        } catch(e: InsufficientImagesException) {
            assert(true)
        }
    }
}