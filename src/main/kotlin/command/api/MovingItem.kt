package command.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface MovingItem {
    val name: String
    val location: IntArray
    val moves: Int
    val value: Int
}
