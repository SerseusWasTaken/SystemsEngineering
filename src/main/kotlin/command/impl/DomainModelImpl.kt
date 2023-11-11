package command.impl

import command.api.DomainModel
import command.api.EventStore
import command.events.ChangeValueEvent
import command.events.CreateEvent
import command.events.DeleteEvent
import command.events.MoveEvent

class DomainModelImpl(
    private val eventStore: EventStore,
    //Visible for testing
    val data: MutableSet<String> = mutableSetOf()
) : DomainModel {

    private fun executeWhenIdIsNotInUse(id: String, block: () -> Unit) =
        if (!data.contains(id))
            block()
        else
            throw IllegalArgumentException("Id is already in use")

    private fun executeWhenIdIsInUse(id: String, block: () -> Unit) =
        if (data.contains(id))
            block()
        else
            throw IllegalArgumentException("No item with given id found")

    override fun createItem(id: String) =
        executeWhenIdIsNotInUse(id) {
            eventStore.storeEvent(CreateEvent(MovingItemImpl(id, intArrayOf(0, 0, 0), 0, 0)))
            data.add(id)
        }


    override fun createItem(id: String, position: IntArray, value: Int) =
        executeWhenIdIsNotInUse(id) {
            eventStore.storeEvent(CreateEvent(MovingItemImpl(id, position, 0, value)))
            data.add(id)
        }

    override fun deleteItem(id: String) =
        executeWhenIdIsInUse(id) {
            eventStore.storeEvent(DeleteEvent(id))
            data.remove(id)
        }

    override fun moveItem(id: String, vector: IntArray) =
        executeWhenIdIsInUse(id) {
            eventStore.storeEvent(MoveEvent(id, vector))
        }

    override fun changeValue(id: String, newValue: Int) =
        executeWhenIdIsInUse(id) {
            eventStore.storeEvent(ChangeValueEvent(id, newValue))
        }
}