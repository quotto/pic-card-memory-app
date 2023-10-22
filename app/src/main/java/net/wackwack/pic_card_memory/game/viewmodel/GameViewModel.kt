package net.wackwack.pic_card_memory.game.viewmodel

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import net.wackwack.pic_card_memory.game.repository.InsufficientImagesException
import net.wackwack.pic_card_memory.game.usecase.GameUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import net.wackwack.pic_card_memory.R
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.model.PlayerType
import net.wackwack.pic_card_memory.game.usecase.ImageRepository
import net.wackwack.pic_card_memory.game.usecase.GameResult
import net.wackwack.pic_card_memory.game.view.GameMode
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil

@HiltViewModel
class GameViewModel @Inject  constructor(
    private val useCase: GameUseCase,
    private val imageRepository: ImageRepository,
    @ApplicationContext context: Context): ViewModel() {

    private val selected = arrayOfNulls<Int>(2)
    private val _imageCards: ArrayList<ImageCard> = arrayListOf()
    val imageCards: List<ImageCard>
        get() = _imageCards
    private val _cardOperationMutex  = Mutex()
    private val _message = MutableSharedFlow<GameMessage>(replay = 0)
    val message: SharedFlow<GameMessage> = _message
    private val _elapsedTime = MutableStateFlow<Long>(0)
    val elapsedTime: StateFlow<Long> = _elapsedTime
    private val _players = MutableStateFlow<List<Player>>(listOf())
    val player1Name: String
        get() = _players.value[0].name
    val player2Name: String
        get() = _players.value[1].name
    private val _player1Score = MutableStateFlow(0)
    val player1Score: StateFlow<Int> = _player1Score.asStateFlow()
    private val _player2Score = MutableStateFlow(0)
    val player2Score: StateFlow<Int> = _player2Score.asStateFlow()
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex

    private val mResources: Resources by lazy { context.resources }

    fun startGame(dispatcher: CoroutineContext, gameMode: GameMode, theme: Resources.Theme) {
        // 初期化
        _imageCards.clear()
        selected[0] = null
        selected[1] = null
        _elapsedTime.value = 0


        val initializedPlayers = when(gameMode) {
            GameMode.SINGLE -> listOf(Player("プレーヤー1", "#FFFFFF", PlayerType.USER))
            GameMode.MULTIPLE -> listOf(
                Player(mResources.getString(R.string.text_of_player1Name),toHexString(mResources.getColor(R.color.player1color, theme)), PlayerType.USER),
                Player(mResources.getString(R.string.text_of_player2Name),toHexString(mResources.getColor(R.color.player2color, theme)), PlayerType.USER)
            )
            else -> listOf(
                Player(mResources.getString(R.string.text_of_youName),toHexString(mResources.getColor(R.color.player1color, theme)), PlayerType.USER),
                Player(mResources.getString(R.string.text_of_comName),toHexString(mResources.getColor(R.color.player2color, theme)), PlayerType.COMPUTER)
            )
        }
        _player1Score.value = 0
        _player2Score.value = 0

        _players.value = initializedPlayers

        viewModelScope.launch(dispatcher) {
            try {
                useCase.startGame(initializedPlayers).collect { cards->
                    // 同じカードをコピーして2倍にする
                    cards.forEach { card ->
                        val bitmapData = imageRepository.loadImageByCard(card)
                        _imageCards.add(
                            ImageCard(card.status, card.uriString).apply {
                                bitmap = bitmapData
                            }
                        )
                    }
                    Log.d(javaClass.simpleName, "Emitted: Start Game")
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

    fun doneCloseCard(cardIndex: Int) {
        _imageCards[cardIndex].status = CardStatus.CLOSE
        if(selected[0] == cardIndex) {
            selected[0] = null
        }
        if(selected[1] == cardIndex) {
            selected[1] = null
        }
        if(selected[0] == null && selected[1] == null) {
            // 2枚カードが閉じられたらロックを解除して次のアクションが取れるようになったことを通知する
            _cardOperationMutex.unlock()
            viewModelScope.launch {
                _message.emit(GameMessage.NextPlayer(_players.value[_currentPlayerIndex.value]))
            }
        }
    }

    fun doneOpenCard() {
        viewModelScope.launch {
            if(selected[0] != null) {
                if (selected[1] != null) {
                    // 2枚目のカードを開いたことを通知する
                    val selectedIndex = selected[1]!!
                    _message.emit(
                        GameMessage.DoneOpen(
                            selectedIndex,
                            _imageCards[selectedIndex].uriString
                        )
                    )

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
                                    _message.emit(GameMessage.DoClose(Pair(first, second)))
                                }
                                if (result.isFailure) {
                                    result.exceptionOrNull()?.let {
                                        Log.e(javaClass.simpleName, it.stackTraceToString())
                                    }
                                }
                            }
                            else -> {
                                // 対戦モードの場合はスコアを更新する
                                if(_players.value.size > 1) {
                                    _player1Score.value = openResult.players[0].getScore()
                                    _player2Score.value = openResult.players[1].getScore()
                                }
                                selected[0] = null
                                selected[1] = null

                                // ペアが揃った場合は次のカードを開くためにロックを解除する
                                _cardOperationMutex.unlock()

                                if (openResult.gameResult == GameResult.GAME_END) {
                                    // クリアを通知
                                    val winner = useCase.getWinner()
                                    _message.emit(GameMessage.GameEnd(winner))
                                } else {
                                    // 次のプレーヤーを通知する、ペアが揃っているため継続になる
                                    _message.emit(GameMessage.NextPlayer(_players.value[_currentPlayerIndex.value]))
                                }
                            }
                        }
                    }
                } else {
                    // 1枚目を開き終わった状態であれば次を開くためにロックを解除する
                    _cardOperationMutex.unlock()

                    // 1枚目を開いたことを通知する
                    val selectedIndex = selected[0]!!
                    _message.emit(
                        GameMessage.DoneOpen(
                            selectedIndex,
                            _imageCards[selectedIndex].uriString
                        )
                    )
                }
            }
        }
    }

    fun startOpenCard(index: Int) {
        Log.d(javaClass.simpleName, "Action@${index}")

        viewModelScope.launch {
            if (_imageCards[index].status == CardStatus.CLOSE) {
                // ロックを取得できた場合のみ処理する
                if(_cardOperationMutex.tryLock()) {
                    if (selected[0] == null || selected[1] == null) {
                        // 選択中のカードが1枚以下の場合のみ開く
                        _imageCards[index].status = CardStatus.OPEN
                        val result = runCatching {
                            _message.emit(GameMessage.DoOpen(index))
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
    data class DoOpen(val target: Int): GameMessage()
    data class DoneOpen(val target: Int, val imageID: String): GameMessage()
    data class DoClose(val target: Pair<Int,Int>): GameMessage()
    data class NextPlayer(val nextPlayer: Player): GameMessage()
    data class GameEnd(val winner: Player?): GameMessage()
    object Start : GameMessage()
    data class Error(val exception: Exception): GameMessage()
}

enum class CardStatus {
    CLOSE,
    OPEN,
}
class ImageCard(var status: CardStatus = CardStatus.CLOSE, val uriString: String) {
    lateinit var bitmap: Bitmap
}