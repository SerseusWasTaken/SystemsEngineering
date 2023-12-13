package handler

import command.events.Event

interface EventHandler {
    fun fetchEvent()
    fun handleEvent(event: Event)

    fun getAverageRecieveTime(): Double
}