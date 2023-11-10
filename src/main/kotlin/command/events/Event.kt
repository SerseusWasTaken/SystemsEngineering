package command.events

import command.api.MovingItem

sealed interface Event

data class CreateEvent(val item: MovingItem): Event
data class ChangeValueEvent(val id: String, val newValue: Int): Event
data class DeleteEvent(val id: String): Event
data class MoveEvent(val id: String, val vector: IntArray): Event

