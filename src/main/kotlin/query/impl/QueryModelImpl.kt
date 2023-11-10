package query.impl

import query.api.MovingItemDTO
import query.api.QueryDatabase
import query.api.QueryModel
import java.util.*

class QueryModelImpl(private val queryDatabase: QueryDatabase) : QueryModel {
    override fun getMovingItems(): Enumeration<MovingItemDTO> {
        return Collections.enumeration(queryDatabase.getItems().values)
    }

    override fun getItemsAtPosition(position: IntArray): Enumeration<MovingItemDTO> {
        return Collections.enumeration(queryDatabase.getItems().values.filter {
            it.location.contentEquals(position)
        })
    }
}