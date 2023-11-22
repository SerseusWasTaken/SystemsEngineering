package query.api

interface QueryDatabase {
    fun changeValue(id: String, value: Int)
    fun createItem(item: MovingItemDTO)
    fun moveLocation(id: String, vector: IntArray)
    fun deleteItem(id: String)
    fun getItem(id: String): MovingItemDTO?
    fun getItems(): Map<String, MovingItemDTO>
}
