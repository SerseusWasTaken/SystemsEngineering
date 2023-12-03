package query.impl

import query.api.MovingItemDTO
import query.api.QueryDatabase
import query.utils.addValues

class QueryDatabaseImpl(val data: MutableMap<String, MovingItemDTO>): QueryDatabase {

    override fun changeValue(id: String, value: Int) {
        val oldItem = data[id]
        oldItem?.apply {
            data.replace(id, MovingItemDTOImpl(name, location, moves, value))
        } ?: throw IllegalArgumentException("No item with given id found")
    }

    override fun createItem(item: MovingItemDTO) {
        data.putIfAbsent(item.name, item)
    }

    override fun moveLocation(id: String, vector: IntArray) {
        val oldItem = data[id]
        oldItem?.apply {
            data.replace(id, MovingItemDTOImpl(name, vector, moves + 1, value))
        }
    }

    override fun deleteItem(id: String) {
        data.remove(id)
    }

    override fun getItem(id: String): MovingItemDTO? {
        return data[id]
    }

    override fun getItems(): Map<String, MovingItemDTO> {
        return data
    }
}