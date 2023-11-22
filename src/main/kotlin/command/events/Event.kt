package command.events

import command.api.MovingItem

sealed interface Event { val id: String}

sealed interface RemoveEvent: Event

data class CreateEvent(val item: MovingItem, override val id: String = item.name): Event
data class ChangeValueEvent(override val id: String, val newValue: Int): Event
data class DeleteEvent(override val id: String): RemoveEvent
data class MoveEvent(override val id: String, val vector: IntArray): Event
data class ReplaceEvent(override val id: String, val itemToMove: String, val vector: IntArray, val value: Int = 0, val doCreateItem: Boolean): RemoveEvent

