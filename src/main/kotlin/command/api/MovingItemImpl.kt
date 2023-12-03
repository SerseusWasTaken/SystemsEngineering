package command.api

import kotlinx.serialization.Serializable

@Serializable
data class MovingItemImpl(
    override val name: String,
    override val location: IntArray,
    override val moves: Int,
    override val value: Int
): MovingItem