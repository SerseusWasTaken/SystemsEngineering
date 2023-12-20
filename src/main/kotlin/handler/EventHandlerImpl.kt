package handler

import command.api.EventStore
import command.events.*
import query.api.QueryDatabase
import query.impl.MovingItemDTOImpl
import java.util.*
import environment
import kafka.Consumer

class EventHandlerImpl(
        private val queryDatabase: QueryDatabase,
        val consumer: Consumer
) : EventHandler {
    private val timestampList = mutableListOf<Long>()

    override fun fetchEvent() {
        consumer.getEvents().forEach {
            println("handling event $it")
            handleEvent(it)
        }
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
                        queryDatabase.createItem(MovingItemDTOImpl(event.itemToMove, event.vector, 0, event.value))
                    else
                        queryDatabase.moveLocation(event.itemToMove, event.vector)
                }
            }

    override fun getAverageRecieveTime(): Double {
        return timestampList.average()
    }
}
