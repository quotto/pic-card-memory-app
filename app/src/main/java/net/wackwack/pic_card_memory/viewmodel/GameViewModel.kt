package net.wackwack.pic_card_memory.viewmodel

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import net.wackwack.pic_card_memory.InsufficientImagesException
import net.wackwack.pic_card_memory.service.GameUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.model.Player
import net.wackwack.pic_card_memory.repository.ImageRepository
import net.wackwack.pic_card_memory.service.GameResult
import net.wackwack.pic_card_memory.view.game.GameMode
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil

@HiltViewModel
class GameViewModel @Inject  constructor(
    private val useCase: GameUseCase,
    private val imageRepository: ImageRepository,
    @ApplicationContext context: Context): ViewModel() {

    val viewModelsCards: ArrayList<ImageCard> = arrayListOf()
    private val selected = arrayOfNulls<Int>(2)

    private val mutex  = Mutex()
    private val _message = MutableSharedFlow<GameMessage>(replay = 1)
    val message: SharedFlow<GameMessage> = _message
    private val _elapsedTime = MutableStateFlow<Long>(0)
    val elapsedTime: StateFlow<Long> = _elapsedTime
    private val _players = MutableStateFlow<List<PlayerStatus>>(listOf())
    val players: StateFlow<List<PlayerStatus>> = _players
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex

    private val mResources: Resources by lazy { context.resources }

    fun startGame(dispatcher: CoroutineContext, gameMode: GameMode, theme: Resources.Theme) {
        // 初期化
        viewModelsCards.clear()
        selected[0] = null
        selected[1] = null
        _elapsedTime.value = 0


        val initializedPlayers = when(gameMode) {
            GameMode.SINGLE -> listOf(Player("プレーヤー1", "#FFFFFF"))
            GameMode.MULTIPLE -> listOf(
                Player("プレーヤー1",toHexString(mResources.getColor(R.color.player1color, theme))),
                Player("プレーヤー2",toHexString(mResources.getColor(R.color.player2color, theme)))
            )
            else -> listOf(
                Player("プレーヤー1",toHexString(Resources.getSystem().getColor(R.color.player1color, theme))),
                Player("COM","#FFFFFF")
            )
        }

        _players.value = initializedPlayers.mapIndexed { index, player ->
            PlayerStatus(player.name, player.getScore())
        }

        viewModelScope.launch(dispatcher) {
            try {
                useCase.startGame(initializedPlayers).collect { cards->
                    // 同じカードをコピーして2倍にする
                    cards.forEach { card ->
                        val bitmapData = imageRepository.loadImageByCard(card)
                        viewModelsCards.add(
                            ImageCard(card.status, card.uriString).apply {
                                bitmap = bitmapData
                            }
                        )
                    }
                    _message.emit(GameMessage.Start)
                }
            } catch(e: InsufficientImagesException) {
                viewModelScope.launch {
                    _message.emit(GameMessage.Error(e))
                }
                return@launch
            }
        }
    }

    private fun toHexString(color: Int): String {
        return String.format("#%06X", 0XFFFFFF and color)
    }

    fun startTimer() {
        viewModelScope.launch {
            _elapsedTime.value = 0
            while (!useCase.checkGameEnd()) {
                delay(1000)
                _elapsedTime.value++
            }
        }
    }

    fun closedCard(cardIndex: Int) {
        viewModelsCards[cardIndex].status = 0
        if(selected[0] == cardIndex) {
            selected[0] = null
        }
        if(selected[1] == cardIndex) {
            selected[1] = null
        }
        if(selected[0] == null && selected[1] == null) {
            // 2枚カードが閉じられたらロックを解除する
            mutex.unlock()
        }
    }

    fun openedCard() {
        viewModelScope.launch {
            if(selected[0] != null) {
                if (selected[1] != null) {
                    // Nullチェック済みのため取り出し
                    val first: Int = selected[0]!!
                    val second: Int = selected[1]!!
                    useCase.updateCardAndPlayerStatus(first, second).collect { openResult ->
                        _currentPlayerIndex.value = openResult.nextPlayerIndex

                        Log.d(javaClass::class.simpleName, openResult.gameResult.toString())
                        when (openResult.gameResult) {
                            GameResult.CARD_CLOSE -> {
                                // 揃わなければカードを閉じる
                                // ロックはカードを閉じるアニメーション終了後に解除するため、ここでは解除しない
                                Log.d(javaClass.simpleName,
                                    "Not Pair(${selected[0]},${selected[1]})")
                                val result = runCatching {
                                    _message.emit(GameMessage.Close(Pair(first, second)))
                                }
                                if (result.isFailure) {
                                    result.exceptionOrNull()?.let {
                                        Log.e(javaClass.simpleName, it.stackTraceToString())
                                    }
                                }
                            }
                            else -> {
                                _players.value = openResult.players.mapIndexed { index, player ->
                                    PlayerStatus(player.name, player.getScore())
                                }
                                selected[0] = null
                                selected[1] = null

                                // ペアが揃った場合は次のカードを開くためにロックを解除する
                                mutex.unlock()

                                if (openResult.gameResult == GameResult.GAME_END) {
                                    // クリアを通知
                                    val winner = useCase.getWinner()
                                    _message.emit(GameMessage.GameEnd(winner))
                                }
                            }
                        }
                    }
                } else {
                    // 1枚目を開き終わった状態であれば次を開くためにロックを解除する
                    mutex.unlock()
                }
            }
        }
    }

    fun openCard(index: Int) {
        Log.d(javaClass.simpleName, "Action@${index}")

        viewModelScope.launch {
            if (viewModelsCards[index].status == 0) {
                // ロックを取得できた場合のみ処理する
                if(mutex.tryLock()) {
                    if (selected[0] == null || selected[1] == null) {
                        // 選択中のカードが1枚以下の場合のみ開く
                        viewModelsCards[index].status = 1
                        val result = runCatching {
                            _message.emit(GameMessage.Open(index))
                        }
                        if (result.isFailure) {
                            result.exceptionOrNull()?.let {
                                Log.e(javaClass.simpleName, it.stackTraceToString())
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
                    Log.d("${javaClass.simpleName}@openCard", "Failed to try lock")
                }
            } else {
                // 開いているカードは写真の詳細を表示する
                _message.emit(GameMessage.Detail(index))
            }
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
    data class GameEnd(val winner: Player?): GameMessage()
    data class Detail(val target: Int): GameMessage()
    object Start : GameMessage()
    data class Error(val exception: Exception): GameMessage()
}

class ImageCard(var status: Int = 0, val uriString: String) {
    lateinit var bitmap: Bitmap
}

class PlayerStatus(val name: String, val score: Int)
