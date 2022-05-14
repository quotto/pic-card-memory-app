package net.wackwack.pic_card_memory.model

import java.lang.IllegalArgumentException

data class Settings(val numOfCard: NumOfCard, val imagePathType: ImagePathType, val imagePath: String)

enum class NumOfCard(val numValue: Int){
    TWELVE(12),
    TWENTY(20),
    THIRTY(30);

    companion object {
        fun convertToValue(v: Int): NumOfCard {
            var result: NumOfCard? = null
            values().forEach { numOfCard ->
                if (numOfCard.numValue == v) {
                    result = numOfCard
                    return@forEach
                }
            }
            if (result == null) {
                throw IllegalArgumentException()
            }
            return result!!
        }
    }
}

enum class ImagePathType(val numValue: Int) {
    EXTERNAL(0),
    SPECIFIED(1);

    companion object{
        fun convertToValue(v: Int): ImagePathType {
            var result: ImagePathType? = null
            values().forEach { imagePathType ->
                if (imagePathType.numValue == v) {
                    result = imagePathType
                    return@forEach
                }
            }
            if (result == null) {
                throw IllegalArgumentException()
            }
            return result!!
        }
    }
}