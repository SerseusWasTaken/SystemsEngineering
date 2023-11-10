package command.api

interface Commands {
    fun createItem(id: String)
    fun createItem(id: String, position: IntArray, value: Int)
    fun deleteItem(id: String)
    fun moveItem(id: String, vector: IntArray)
    fun changeValue(id: String, newValue: Int)
}