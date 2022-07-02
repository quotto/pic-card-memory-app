package net.wackwack.pic_card_memory.game.model.computer

import net.wackwack.pic_card_memory.game.view.ComputerLevel
import net.wackwack.pic_card_memory.game.viewmodel.GameViewModel

class ComputerFactory {
    companion object {
        fun createComputer(level: ComputerLevel, gameViewModel: GameViewModel): ComputerInterface {
            return when(level) {
                ComputerLevel.EASY -> EasyComputer(gameViewModel)
                ComputerLevel.NORMAL -> NormalComputer(gameViewModel)
                ComputerLevel.HARD -> HardComputer(gameViewModel)
                else -> throw IllegalArgumentException()
            }
        }
    }
}