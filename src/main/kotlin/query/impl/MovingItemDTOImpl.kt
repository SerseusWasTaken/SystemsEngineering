package query.impl

import command.api.MovingItem
import query.api.MovingItemDTO

class MovingItemDTOImpl(
    override val name: String,
    override val location: IntArray,
    override val moves: Int,
    override val value: Int
) : MovingItemDTO {

    companion object {
        fun fromMovingItem(item: MovingItem) = MovingItemDTOImpl(item.name, item.location, item.moves, item.value)
    }
}