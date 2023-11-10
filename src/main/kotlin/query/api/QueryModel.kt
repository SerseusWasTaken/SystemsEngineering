package query.api

import java.util.*

interface QueryModel {
    fun getMovingItems(): Enumeration<MovingItemDTO>
    fun getItemsAtPosition(position: IntArray): Enumeration<MovingItemDTO>
}