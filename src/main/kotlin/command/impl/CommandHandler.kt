package command.impl

import command.api.Commands
import command.api.DomainModel

class CommandHandler(private val domainModel: DomainModel): Commands {

    override fun createItem(id: String) {
        domainModel.createItem(id)
    }

    override fun createItem(id: String, position: IntArray, value: Int) {
        domainModel.createItem(id, position, value)
    }

    override fun deleteItem(id: String) {
        domainModel.deleteItem(id)
    }

    override fun moveItem(id: String, vector: IntArray) {
        domainModel.moveItem(id, vector)
    }

    override fun changeValue(id: String, newValue: Int) {
        domainModel.changeValue(id, newValue)
    }
}
