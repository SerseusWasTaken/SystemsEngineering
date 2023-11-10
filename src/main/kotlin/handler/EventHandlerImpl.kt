package handler

import command.api.EventStore
import command.events.*
import query.api.QueryDatabase
import query.impl.MovingItemDTOImpl

class EventHandlerImpl(
    private val eventStore: EventStore,
    private val queryDatabase: QueryDatabase
) : EventHandler {
    override fun fetchEvent() {

    }

    override fun handleEvent(event: Event): Unit =
        when(event) {
            is CreateEvent -> queryDatabase.createItem(MovingItemDTOImpl.fromMovingItem(event.item))
            is ChangeValueEvent -> queryDatabase.changeValue(event.id, event.newValue)
            is DeleteEvent -> queryDatabase.deleteItem(event.id)
            is MoveEvent -> queryDatabase.moveLocation(event.id, event.vector)
        }

}