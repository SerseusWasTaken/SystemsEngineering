package command.api

interface DomainModel {
    fun createItem(id: String)
    fun createItem(id: String, position: IntArray, value: Int = 0)
    fun deleteItem(id: String)
    fun moveItem(id: String, vector: IntArray)
    fun changeValue(id: String, newValue: Int)
}