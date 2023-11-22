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
        handleEvent(eventStore.pop())
    }

    override fun handleEvent(event: Event): Unit =
            when (event) {
                is CreateEvent -> queryDatabase.createItem(MovingItemDTOImpl.fromMovingItem(event.item))
                is ChangeValueEvent -> queryDatabase.changeValue(event.id, event.newValue)
                is MoveEvent -> queryDatabase.moveLocation(event.id, event.vector)
                is DeleteEvent -> queryDatabase.deleteItem(event.id)
                is ReplaceEvent -> {
                    queryDatabase.deleteItem(event.id)
                    if (event.doCreateItem)
                        queryDatabase.createItem(MovingItemDTOImpl(event.id, event.vector, 0, event.value))
                    else
                        queryDatabase.moveLocation(event.itemToMove, event.vector)
                }
            }
}
