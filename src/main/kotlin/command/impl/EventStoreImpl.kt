package command.impl

import command.api.EventStore
import command.events.Event
import java.util.concurrent.BlockingQueue

class EventStoreImpl(val queue: BlockingQueue<Event>, val allEvents: MutableCollection<Event>): EventStore {
    override fun storeEvent(event: Event) {
        queue.add(event)
        allEvents.add(event)
    }

    override fun pop(): Event =
        queue.take()

    override fun getAllEvents(): List<Event> =
        allEvents.toList()
}