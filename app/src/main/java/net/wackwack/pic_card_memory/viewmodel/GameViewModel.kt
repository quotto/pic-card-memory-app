package net.wackwack.pic_card_memory.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import net.wackwack.pic_card_memory.InsufficientImagesException
import net.wackwack.pic_card_memory.model.Card
import net.wackwack.pic_card_memory.model.GameUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.wackwack.pic_card_memory.repository.ImageRepository
import net.wackwack.pic_card_memory.repository.SettingsRepository
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil

@HiltViewModel
class GameViewModel @Inject  constructor(
    private val imageRepository: ImageRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext context: Context): ViewModel() {

    val viewModelsCards: ArrayList<ViewModelsCard> = arrayListOf()
    private val selected = arrayOfNulls<Int>(2)
    private var numOfHandling: Int = 0

    private val mutex  = Mutex()
    private val _message = MutableSharedFlow<GameMessage>(replay = 1)
    val message: SharedFlow<GameMessage> = _message
    private val _elapsedTime = MutableStateFlow<Long>(0)
    val elapsedTime: StateFlow<Long> = _elapsedTime
    private val mContentResolver: ContentResolver by lazy { context.contentResolver }


    private lateinit var useCase: GameUseCase

    fun setupGame(dispatcher: CoroutineContext) {
        // 初期化
        viewModelsCards.clear()
        selected[0] = null
        selected[1] = null
        numOfHandling = 0

        viewModelScope.launch(dispatcher) {
            try {
                val rawCardList = imageRepository.selectCardsBySettings(
                    settingsRepository.getSettings()
                )
                // 同じカードをコピーして2倍にする
                rawCardList.forEach { card ->
                    val bitmapData = readBitmapFromUri(Uri.parse(card.uriString))
                    viewModelsCards.add(
                        ViewModelsCard(card.id,card.uriString,card.status).apply {
                            bitmap = bitmapData
                        }
                    )
                    viewModelsCards.add(
                        ViewModelsCard(card.id,card.uriString,card.status).apply {
                            bitmap = bitmapData
                        }
                    )
                }
                viewModelsCards.shuffle()
            } catch(e: InsufficientImagesException) {
                viewModelScope.launch {
                    _message.emit(GameMessage.Error(e))
                }
                return@launch
            }

            useCase = GameUseCase(viewModelsCards.count())

            _message.emit(GameMessage.Start)
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            _elapsedTime.value = 0
            while (!useCase.isEnd()) {
                delay(1000)
                _elapsedTime.value++
            }
            Log.d(javaClass.simpleName, "Game is end")
        }
    }

    fun unlockCardHandle() {
        viewModelScope.launch {
            mutex.withLock {
                numOfHandling -= 1
                Log.d(javaClass.simpleName, "numOfHandling:$numOfHandling")
            }
            if(selected[0] != null && selected[1] != null) {
                // Nullチェック済みのため取り出し
                val first: Int = selected[0]!!
                val second: Int = selected[1]!!
                // ペアが揃ったことをチェック
                if (!useCase.isPair(viewModelsCards[first], viewModelsCards[second])) {
                    // 揃わなければカードを閉じる
                    Log.d(javaClass.simpleName, "Not Pair(${selected[0]},${selected[1]})")
                    val result = runCatching {
                        mutex.withLock {
                            numOfHandling += 2
                        }
                        _message.emit(GameMessage.Close(Pair(first, second)))
                    }
                    if (result.isFailure) {
                        result.exceptionOrNull()?.let {
                            Log.e(javaClass.simpleName, it.stackTraceToString())
                        }
                    } else {
                        mutex.withLock {
                            viewModelsCards[first].status = 0
                            viewModelsCards[second].status = 0
                        }
                    }
                } else {
                    // ペアが揃ったらクリア状態をチェック
                    if(useCase.isEnd()) {
                        // クリアを通知
                        _message.emit(GameMessage.Clear)
                    }
                }
                mutex.withLock {
                    selected[0] = null
                    selected[1] = null
                }
            }
        }
    }

    fun openCard(index: Int) {
        Log.d(javaClass.simpleName, "Action@${index}")

        if (numOfHandling == 0) {
            if (viewModelsCards[index].status == 0) {
                if (selected[0] == null || selected[1] == null) {
                    // 選択中のカードが1枚以下の場合のみ開く
                    viewModelsCards[index].status = 1
                    viewModelScope.launch {
                        mutex.withLock {
                            numOfHandling++
                        }
                        val result = runCatching {
                            _message.emit(GameMessage.Open(index))
                        }
                        if (result.isFailure) {
                            result.exceptionOrNull()?.let {
                                Log.e(javaClass.simpleName, it.stackTraceToString())
                            }
                        }
                    }
                    if (selected[0] == null) {
                        // 1枚目が未選択なら選択済みにする
                        selected[0] = index
                    } else if (selected[1] == null) {
                        // 1枚目が選択済みなら2枚目を選択済みにする
                        selected[1] = index
                    }
                }
            } else {
                // 開いているカードは写真の詳細を表示する
                viewModelScope.launch {
                    _message.emit(GameMessage.Detail(index))
                }
            }
        }
    }

    private fun readBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContentResolver.loadThumbnail(uri, Size(640, 480),null)
        } else {
            MediaStore.Images.Media.getBitmap(mContentResolver,uri)
        }
    }

    fun elapsedTimeToString(): String {
        var elapsedTimeText = ""
        val min = ceil((elapsedTime.value/60).toDouble()).toInt()
        val sec = if(elapsedTime.value < 60) elapsedTime.value else elapsedTime.value % 60
        elapsedTimeText += if(min<10) {
            "0$min"
        } else {
            min.toString()
        }
        elapsedTimeText+=":"
        elapsedTimeText += if(sec < 10) {
            "0$sec"
        } else {
            sec.toString()
        }
        return elapsedTimeText
    }
}

sealed class GameMessage {
    data class Open(val target: Int): GameMessage()
    data class Close(val target: Pair<Int,Int>): GameMessage()
    object Clear : GameMessage()
    data class Detail(val target: Int): GameMessage()
    object Start : GameMessage()
    data class Error(val exception: Exception): GameMessage()
}

class ViewModelsCard(id: Long, uriString: String, status: Int = 0) : Card(id, uriString, status) {
    lateinit var bitmap: Bitmap
}