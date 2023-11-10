package command.impl

import command.api.EventStore
import command.events.Event
import java.util.concurrent.BlockingQueue

class EventStoreImpl(private val queue: BlockingQueue<Event>): EventStore {
    override fun storeEvent(event: Event) {
        queue.add(event)
    }

    override fun pop(): Event  = queue.take()

}