package command.api

import command.events.Event

interface EventStore {
    fun storeEvent(event: Event)
    fun pop(): Event
    fun getAllEvents(): List<Event>
}
