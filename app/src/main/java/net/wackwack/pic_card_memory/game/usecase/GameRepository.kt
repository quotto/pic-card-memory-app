package net.wackwack.pic_card_memory.game.usecase

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import net.wackwack.pic_card_memory.game.model.GameBoard
import net.wackwack.pic_card_memory.game.model.Player
import net.wackwack.pic_card_memory.game.repository.GameRepositoryImpl

interface GameRepository {
    fun registerGameBoard(gameBoard: GameBoard)

    fun getCurrentGameBoard(): GameBoard

    fun registerPlayers(players: List<Player>)

    fun updatePlayerStatus(playerIndex: Int, player: Player)

    fun getPlayers(): List<Player>
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class GameRepositoryModule {
    @ActivityRetainedScoped
    @Binds
    abstract fun bindSettingsRepositoryImpl(impl: GameRepositoryImpl): GameRepository
}