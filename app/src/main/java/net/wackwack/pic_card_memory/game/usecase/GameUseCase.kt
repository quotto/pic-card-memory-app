package net.wackwack.pic_card_memory.game.usecase

import android.util.Log
import kotlinx.coroutines.flow.*
import net.wackwack.pic_card_memory.game.model.Card
import net.wackwack.pic_card_memory.game.model.GameBoard
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.settings.model.BGMVolume
import net.wackwack.pic_card_memory.settings.usecase.SettingsRepository
import javax.inject.Inject

class GameUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val settingsRepository: SettingsRepository,
    private val imageRepository: ImageRepository
) {

    fun startGame(players: List<Player>): Flow<List<Card>> {
        val rawCards = imageRepository.selectCardsBySettings(
            settingsRepository.getSettings()
        )

        val cards = arrayListOf<Card>()
        // 同じカードをコピーして2倍にする
        rawCards.forEach { card ->
            repeat(2) {
                cards.add(
                    Card(card.id, card.uriString, card.status)
                )
            }
        }
        cards.shuffle()

        val newGameBoard = GameBoard(cards, players)
        gameRepository.registerGameBoard(newGameBoard)
        return flow {
            emit(newGameBoard.cards)
        }
    }

    /*
    ペア判定
     */
    fun updateCardAndPlayerStatus(card1: Int, card2: Int): Flow<OpenResult> {
        var gameResult = GameResult.CARD_CLOSE

        val gameBoard = gameRepository.getCurrentGameBoard()

        if(gameBoard.compare(card1,card2)) {
            gameBoard.openCard(card1)
            gameBoard.openCard(card2)
            gameBoard.addPointToCurrentPlayer()
            gameResult = if(gameBoard.isGameEnd()) GameResult.GAME_END else GameResult.CARD_OPEN
        } else {
            gameBoard.changePlayer()
        }
        return flow {
            emit(
                OpenResult(
                gameBoard.players,
                gameBoard.getCurrentPlayerIndex(),
                gameResult
            )
            )
        }
    }

    /*
    ゲーム終了の判定
     */
    fun checkGameEnd(): Boolean {
        return gameRepository.getCurrentGameBoard().isGameEnd()
    }

    fun getWinner(): Player? {
        return gameRepository.getCurrentGameBoard().findWinner()
    }

    /*
    BGMをONにして情報を保存する
     */
    suspend fun setBGMOn() {
        if(!settingsRepository.updateBGMVolume(BGMVolume.MAX)) {
            Log.e(javaClass.simpleName,"Failed to save BGMVolume ON")
        }
    }

    /*
    BGMをOFFにして情報を保存する
     */
    suspend fun setBGMOff() {
        if (!settingsRepository.updateBGMVolume(BGMVolume.MIN)) {
            Log.e(javaClass.simpleName, "Failed to save BGMVolume OFF")
        }
    }

    /*
    保存されているBGMの設定を取得する
     */
    suspend fun getSavedBGM(): BGMVolume {
        return settingsRepository.getBGMVolume()
    }
}