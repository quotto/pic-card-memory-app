package net.wackwack.pic_card_memory.game.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import net.wackwack.pic_card_memory.game.model.Card
import net.wackwack.pic_card_memory.game.usecase.ImageRepository
import net.wackwack.pic_card_memory.settings.model.ImagePathType
import net.wackwack.pic_card_memory.settings.model.Settings
import net.wackwack.pic_card_memory.game.viewmodel.CardStatus
import java.security.SecureRandom
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(@ApplicationContext val context: Context):
    ImageRepository {
    override fun selectCardsBySettings(settings: Settings): List<Card> {
        val requiredCard = settings.numOfCard.numValue.div(2)
        val result =  when (settings.imagePathType) {
            ImagePathType.EXTERNAL -> loadFromExternalStorage(requiredCard)
            ImagePathType.SPECIFIED -> loadFromDocumentTree(settings.imagePath,requiredCard)
        }
        if(result.size < requiredCard) {
            throw InsufficientImagesException()
        }
        return result
    }

    override fun loadImageByCard(card: Card): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(Uri.parse(card.uriString), Size(640, 480),null)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver,Uri.parse(card.uriString))
        }
    }

    @SuppressLint("Range")
    private fun loadFromExternalStorage(requiredCard: Int): List<Card> {
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Images.ImageColumns.RELATIVE_PATH)
        } else {
            context.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null)
        }

        Log.d(javaClass.simpleName, "EXTERNAL_STORAGE Count: ${cursor?.count}")

        val result = arrayListOf<Card>()

        cursor?.let { fixCursor ->
            if (fixCursor.count >= requiredCard) {
                val selectedIndex = generateRandomIndex(fixCursor.count, requiredCard)
                selectedIndex.forEach {index->
                    fixCursor.moveToPosition(index)
                    val id =
                        fixCursor.getString(fixCursor.getColumnIndex(MediaStore.Images.Media._ID))
                            .toLong()
                    result.add(
                        Card(
                        id,
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id).toString(),
                        CardStatus.CLOSE)
                    )
                }
            }
            fixCursor.close()
        }
        return result.toList()
    }

    private fun loadFromDocumentTree(documentRoot: String, requiredCard: Int): List<Card> {
        val result = arrayListOf<Card>()

        DocumentFile.fromTreeUri(context, documentRoot.toUri())?.let { root ->
            val files = root.listFiles().filter { file ->
                val pattern = Regex("^image/.+")
                file.type?.contains(pattern) == true
            }

            if(files.size >= requiredCard) {
                val selectedIndex = generateRandomIndex(files.size, requiredCard)
                selectedIndex.forEach { index ->
                    result.add(Card(files[index].uri.hashCode().toLong(), files[index].uri.toString()))
                }
            }
        }
        return result.toList()
    }

    private fun generateRandomIndex(size: Int, requiredIndex: Int): List<Int> {
        val selections = arrayListOf<Int>()
        val secureRandom = SecureRandom()
        (0 until requiredIndex).forEach { _ ->
            while (true) {
                val rand = secureRandom.nextInt(size-1)
                if (!selections.contains(rand)) {
                    selections.add(rand)
                    break
                }
            }
        }
        selections.sort()
        Log.d(javaClass.simpleName, "Selected Index: ${selections.joinToString(",")}")
        return selections
    }
}