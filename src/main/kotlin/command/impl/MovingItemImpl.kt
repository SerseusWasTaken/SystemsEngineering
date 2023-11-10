package command.impl

import command.api.MovingItem

data class MovingItemImpl(
    override val name: String,
    override val location: IntArray,
    override val moves: Int,
    override val value: Int
): MovingItem